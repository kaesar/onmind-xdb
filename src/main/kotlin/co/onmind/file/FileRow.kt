package co.onmind.file

import co.onmind.xy.XYAny

const val FILES_SHEET = "FILES"
const val FILES_SPEC = "file_id=string,name=string,mime=string,size=long,checksum=string,s3_key=string,uploaded_by=string,created_at=timestamp,tags=string"

/** Convenience mapping from a FILES metadata row (XYAny) to named fields. */
object FileRow {

    fun fileId(row: MutableMap<String, Any?>): String = row["any01"]?.toString() ?: ""

    fun s3Key(row: MutableMap<String, Any?>): String = row["any04"]?.toString() ?: ""

    fun name(row: MutableMap<String, Any?>): String = row["any02"]?.toString() ?: ""

    fun mime(row: MutableMap<String, Any?>): String = row["any15"]?.toString() ?: "application/octet-stream"

    fun size(row: MutableMap<String, Any?>): Long = row["any14"]?.toString()?.toLongOrNull() ?: 0L

    fun checksum(row: MutableMap<String, Any?>): String = row["any13"]?.toString() ?: ""

    fun tags(row: MutableMap<String, Any?>): String = row["any03"]?.toString() ?: ""

    fun uploadedBy(row: MutableMap<String, Any?>): String = row["anyby"]?.toString() ?: ""

    fun createdAt(row: MutableMap<String, Any?>): String = row["anyat"]?.toString() ?: row["anyon"]?.toString() ?: ""

    /** Public JSON view: id + named fields (any01 computed by the XYAny insert). */
    fun toView(row: MutableMap<String, Any?>): Map<String, Any?> = mapOf(
        "id" to (row["id"]?.toString() ?: ""),
        "fileId" to fileId(row),
        "name" to name(row),
        "mime" to mime(row),
        "size" to size(row),
        "checksum" to checksum(row),
        "s3Key" to s3Key(row),
        "uploadedBy" to uploadedBy(row),
        "createdAt" to createdAt(row),
        "tags" to tags(row)
    )

    /** XYAny produced from request fields; any01 is derived from name + sheet. */
    fun toRow(
        fileId: String,
        name: String,
        mime: String?,
        size: Long?,
        checksum: String?,
        s3Key: String,
        tags: String?,
        uploadedBy: String
    ): XYAny = XYAny(
        id = "",
        anyxy = FILES_SHEET,
        anyis = null,
        any00 = null,
        any01 = null,
        any02 = name,
        any03 = tags,
        any04 = s3Key,
        any05 = null, any06 = null,
        any13 = checksum,
        any14 = size?.toString(),
        any15 = mime,
        any10 = null, any11 = null, any12 = null,
        any16 = null, any17 = null, any18 = null, any19 = null,
        any20 = null, any21 = null, any22 = null, any23 = null, any24 = null, any25 = null,
        any26 = null, any27 = null, any28 = null, any29 = null,
        any30 = null, any31 = null, any32 = null, any33 = null, any34 = null, any35 = null,
        any36 = null, any37 = null, any38 = null, any39 = null,
        any40 = null, any41 = null, any42 = null, any43 = null, any44 = null, any45 = null,
        any46 = null, any47 = null, any48 = null, any49 = null,
        any50 = null, any51 = null, any52 = null, any53 = null, any54 = null, any55 = null,
        any56 = null, any57 = null, any58 = null, any59 = null, any60 = null,
        anydo = null, anyto = null, anyio = null, anyas = null,
        anyof = null,
        anyby = uploadedBy,
        anyon = null,
        anyat = null
    )
}