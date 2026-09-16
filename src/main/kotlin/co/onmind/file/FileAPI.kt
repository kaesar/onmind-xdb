package co.onmind.file

import co.onmind.db.RDB
import co.onmind.util.AbstractIds
import co.onmind.util.JsonMapper
import org.http4k.core.ContentType
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path as PathLens
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.io.InputStream
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * REST API `/file` (FILES feature). All endpoints require authentication via
 * the global auth filter; scope checks (file:read / file:write) happen in the
 * route handler against the authenticated user header.
 *
 * Endpoints:
 *   POST   /file/presign-upload  -> {uploadUrl, fileId, s3Key, expiresAt, mode}
 *   POST   /file/confirm         -> registers metadata row in FILES sheet
 *   GET    /file/{id}            -> 302 to presigned GET (or 200 stream in local mode)
 *   GET    /file/{id}/url        -> {downloadUrl}
 *   DELETE /file/{id}            -> deletes object + metadata
 *   GET    /file                 -> list with filters (mime, user, tags, date)
 *   PATCH  /file/{id}            -> update metadata (name, tags)
 *   GET    /file/config          -> {enabled, mode, bucket, prefix}
 */
class FileAPI(private val service: FileService, private val ttl: Duration = Duration.ofHours(1)) {

    private val xdb = RDB()
    private val mapper = JsonMapper.instance
    private val storage: FileStorage? = service as? FileStorage
    private val prefix = "files"

    fun routes(): org.http4k.routing.RoutingHttpHandler = routes(
        "/file/presign-upload" bind Method.POST to ::presignUpload,
        "/file/confirm" bind Method.POST to ::confirmUpload,
        "/file/config" bind Method.GET to ::config,
        "/file" bind Method.GET to ::list,
        "/file/{id}" bind Method.GET to ::download,
        "/file/{id}/url" bind Method.GET to ::downloadUrl,
        "/file/{id}" bind Method.DELETE to ::delete,
        "/file/{id}" bind Method.PATCH to ::patch,
        "/file/{id}" bind Method.PUT to ::serverPut
    )

    private fun authUser(req: Request): String = req.header("X-Auth-User") ?: "anonymous"

    private fun requireWrite(req: Request): Response? =
        if (authUser(req) == "anonymous") jsonError("file:write required", Status.FORBIDDEN) else null

    private fun json(body: Map<String, Any?>, status: Status = Status.OK): Response =
        Response(status)
            .header("Content-Type", ContentType.APPLICATION_JSON.value)
            .body(mapper.writeValueAsString(body))

    private fun jsonError(message: String, status: Status = Status.BAD_REQUEST): Response =
        json(mapOf("ok" to false, "error" to message), status)

    /** POST /file/presign-upload {name, mime, size, tags?} */
    private fun presignUpload(req: Request): Response {
        requireWrite(req)?.let { return it }
        val body = try {
            mapper.readTree(req.bodyString())
        } catch (ex: Exception) {
            return jsonError("Invalid JSON body: ${ex.message}")
        }
        val name = body.get("name")?.asText()?.trim()
        if (name.isNullOrBlank()) return jsonError("'name' is required")
        val mime = body.get("mime")?.asText()?.takeIf { it.isNotBlank() } ?: "application/octet-stream"
        val size = body.get("size")?.asLong() ?: 0L
        val tags = body.get("tags")?.asText()?.takeIf { it.isNotBlank() }

        val fileId = AbstractIds.uuid()  // browser-facing id
        val uuidDir = fileId.take(13)
        val safeName = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val key = "$prefix/$uuidDir/$safeName"

        val expiresAt = LocalDateTime.now().plus(ttl)
        return json(
            mapOf(
                "ok" to true,
                "uploadUrl" to service.presignPut(key, ttl, mime),
                "fileId" to fileId,
                "s3Key" to key,
                "name" to name,
                "mime" to mime,
                "size" to size,
                "tags" to tags.orEmpty(),
                "mode" to (if (storage != null) "local" else "s3"),
                "expiresAt" to expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        )
    }

    /**
     * POST /file/confirm {fileId, s3Key, name, checksum, mime, size, tags?}
     * Registers metadata in FILES. In local mode the browser PUTs the binary to
     * the server instead; confirm just records metadata (binary already stored).
     */
    private fun confirmUpload(req: Request): Response {
        requireWrite(req)?.let { return it }
        val body = try {
            mapper.readTree(req.bodyString())
        } catch (ex: Exception) {
            return jsonError("Invalid JSON body: ${ex.message}")
        }
        val user = authUser(req)
        val fileId = body.get("fileId")?.asText() ?: return jsonError("'fileId' is required")
        val s3Key = body.get("s3Key")?.asText() ?: return jsonError("'s3Key' is required")
        val name = body.get("name")?.asText()?.trim()
            ?: s3Key.substringAfterLast('/')
        val checksum = body.get("checksum")?.asText()?.takeIf { it.isNotBlank() }
        val mime = body.get("mime")?.asText()?.takeIf { it.isNotBlank() }
        val size = body.get("size")?.asLong() ?: 0L
        val tags = body.get("tags")?.asText()?.takeIf { it.isNotBlank() }

        val meta = mutableMapOf<String, Any?>(
            "any02" to name,
            "any03" to tags,
            "any04" to s3Key,
            "any13" to checksum,
            "any14" to size.toString(),
            "any15" to mime
        )
        val rowId = FileMeta.insert(meta, user)
        if (rowId == null) return jsonError("Failed to register file metadata", Status.INTERNAL_SERVER_ERROR)
        val row = FileMeta.findById(rowId)
        return json(mapOf("ok" to true, "id" to rowId, "file" to (row?.let { FileRow.toView(it) } ?: emptyMap<String, Any?>())))
    }

    /** GET /file/{id} → 302 presigned GET (s3) or 200 stream (local). */
    fun download(req: Request): Response {
        val id = PathLens.of("id")(req)
        // Accept both the XDB row id and the presign-issued fileId (UUID).
        val row = resolveRow(id) ?: return jsonError("File not found", Status.NOT_FOUND)
        val key = FileRow.s3Key(row)
        val name = FileRow.name(row)
        val mime = FileRow.mime(row)

        if (storage != null) {
            val stream = storage.stream(key) ?: return jsonError("Blob missing", Status.NOT_FOUND)
            return Response(Status.OK)
                .header("Content-Type", mime)
                .header("Content-Disposition", "attachment; filename=\"$name\"")
                .body(stream, -1L)
        }

        val url = service.presignGet(key, ttl)
        return Response(Status.FOUND).header("Location", url)
    }

    /** GET /file/{id}/url → {downloadUrl, expiresAt} */
    fun downloadUrl(req: Request): Response {
        val id = PathLens.of("id")(req)
        val row = resolveRow(id) ?: return jsonError("File not found", Status.NOT_FOUND)
        val url = service.presignGet(FileRow.s3Key(row), ttl)
        val expiresAt = LocalDateTime.now().plus(ttl)
        return json(mapOf("ok" to true, "downloadUrl" to url, "expiresAt" to expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
    }

    /** Row lookup that accepts both the XDB row id and the presign fileId (UUID). */
    private fun resolveRow(id: String): MutableMap<String, Any?>? {
        FileMeta.findById(id)?.let { return it }
        return FileMeta.list().firstOrNull { row ->
            FileRow.s3Key(row).contains("/$id/") || FileRow.s3Key(row) == "files/$id"
        }
    }

    /** DELETE /file/{id} */
    fun delete(req: Request): Response {
        requireWrite(req)?.let { return it }
        val id = PathLens.of("id")(req)
        val row = FileMeta.findById(id) ?: return jsonError("File not found", Status.NOT_FOUND)
        val key = FileRow.s3Key(row)

        val deleted = if (storage != null) storage.delete(key) else service.delete(key)
        if (!deleted) warn("Blob delete reported failure; removing metadata anyway", mapOf("id" to id, "s3Key" to key))
        FileMeta.delete(id)
        return json(mapOf("ok" to true, "deleted" to true))
    }

    /** PATCH /file/{id} {name?, tags?} */
    fun patch(req: Request): Response {
        requireWrite(req)?.let { return it }
        val id = PathLens.of("id")(req)
        val row = FileMeta.findById(id) ?: return jsonError("File not found", Status.NOT_FOUND)
        val body = try {
            mapper.readTree(req.bodyString())
        } catch (ex: Exception) {
            return jsonError("Invalid JSON body: ${ex.message}")
        }
        val name = body.get("name")?.asText()?.trim()?.takeIf { it.isNotBlank() }
        val tags = body.get("tags")?.asText()

        val any02 = name ?: (row["any02"]?.toString() ?: "")
        // any01 is UNIQUE: keep the row id suffix like FileMeta.insert does.
        val any01 = "${any02.uppercase()}~FILES~$id"
        var sets = "any02='$any02', any01='$any01'"
        if (tags != null) sets += ", any03='$tags'"
        xdb.forUpdate("UPDATE xyany SET $sets WHERE id='$id'")
        xdb.savePointAny(row)
        val updated = FileMeta.findById(id)
        return json(mapOf("ok" to true, "file" to (updated?.let { FileRow.toView(it) } ?: emptyMap<String, Any?>())))
    }

    /** GET /file?mime=&user=&tags=&from=&to=&q= */
    fun list(req: Request): Response {
        val mime = req.query("mime")
        val user = req.query("user")
        val tags = req.query("tags")
        val from = req.query("from")
        val to = req.query("to")
        val q = req.query("q")
        val limit = req.query("limit")?.toIntOrNull() ?: onmindxdb.queryLimit

        val clauses = mutableListOf<String>()
        if (!mime.isNullOrBlank()) clauses.add("any15 LIKE '${mime}%'")
        if (!user.isNullOrBlank()) clauses.add("anyby = '$user'")
        if (!tags.isNullOrBlank()) clauses.add("any03 LIKE '%$tags%'")
        if (!from.isNullOrBlank()) clauses.add("anyat >= '$from'")
        if (!to.isNullOrBlank()) clauses.add("anyat <= '$to'")
        if (!q.isNullOrBlank()) clauses.add("(any02 LIKE '%$q%' OR any01 LIKE '%$q%')")
        val where = if (clauses.isEmpty()) null else clauses.joinToString(" AND ")

        val rows = FileMeta.list(where)
        val data = rows.take(limit).map { FileRow.toView(it) }
        return json(mapOf("ok" to true, "total" to rows.size, "data" to data))
    }

    /** GET /file/config */
    private fun config(req: Request): Response =
        json(mapOf("enabled" to true, "mode" to (if (storage != null) "local" else "s3"), "bucket" to service.bucket, "prefix" to prefix))

    /** Local-mode server-side PUT target (browser uploads to /file/{id} with PUT). */
    fun serverPut(req: Request): Response {
        requireWrite(req)?.let { return it }
        // The client uploads to the presign-issued fileId (UUID) before confirm;
        // metadata is not registered yet, so seed a placeholder row lazily.
        val id = PathLens.of("id")(req)
        var row = FileMeta.findById(id)
        if (row == null) {
            val meta = co.onmind.file.FileRow.toRow(
                fileId = id,
                name = "pending-${id.take(8)}",
                mime = req.header("Content-Type"),
                size = null,
                checksum = null,
                s3Key = "files/${id.take(13)}/pending.bin",
                tags = null,
                uploadedBy = authUser(req)
            ).let { r ->
                mutableMapOf<String, Any?>(
                    "any02" to r.any02,
                    "any03" to r.any03,
                    "any04" to r.any04,
                    "any13" to r.any13,
                    "any14" to r.any14,
                    "any15" to r.any15
                )
            }
            val newId = FileMeta.insert(meta, authUser(req)) ?: return jsonError("Upload failed", Status.INTERNAL_SERVER_ERROR)
            row = FileMeta.findById(newId)
        }
        val key = FileRow.s3Key(row ?: return jsonError("File not found", Status.NOT_FOUND))
        val bodyBytes: ByteArray = req.bodyString().toByteArray()
        val inputStream: InputStream = bodyBytes.inputStream()
        val ok = storage?.upload(key, inputStream, FileRow.mime(row)) ?: false
        inputStream.close()
        if (!ok) return jsonError("Upload failed", Status.INTERNAL_SERVER_ERROR)
        return json(mapOf("ok" to true, "id" to id))
    }

    companion object {
        /** Trace.logWarn does not accept a context map; log inline here. */
        private fun warn(msg: String, ctx: Map<String, Any?>) {
            co.onmind.util.Trace.logWarn("$msg ${ctx.entries.joinToString { "${it.key}=${it.value}" }}")
        }
    }
}