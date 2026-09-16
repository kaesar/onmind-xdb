package co.onmind.file

import co.onmind.util.Rote
import co.onmind.util.Trace
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Local folder blob backend (<app.local>/xy/files). Serves as the default when
 * no S3/RustFS endpoint is configured (file.enabled=+ without s3.endpoint),
 * so the File API and the UI remain testable without external storage.
 */
class FileStorage(
    private val root: String = run {
        val base = onmindxdb.dbfile?.let { File(it).parentFile } ?: File(Rote.path)
        File(base, "files").absolutePath
    }
) : FileService {

    override val bucket: String = "local"
    override val endpoint: String? = null
    override val region: String = "local"

    /** Local presigned URLs are not signed; they resolve through `/file/{id}`. */
    override fun presignGet(key: String, expiresIn: Duration): String {
        val id = key.substringAfterLast('/')
        return "/file/$id?expires=${Instant.now().plus(expiresIn).toEpochMilli()}"
    }

    /** Local PUTs go through POST /file/confirm (server-side write). */
    override fun presignPut(key: String, expiresIn: Duration, contentType: String?): String =
        "/file/local-put?key=${java.net.URLEncoder.encode(key, "UTF-8")}"

    override fun presignDelete(key: String, expiresIn: Duration): String =
        "/file/local-delete?key=${java.net.URLEncoder.encode(key, "UTF-8")}"

    override fun delete(key: String): Boolean {
        val file = resolve(key) ?: return false
        return try {
            file.delete()
        } catch (ex: Exception) {
            Trace.logError("Local file delete failed", ex, mapOf("key" to key))
            false
        }
    }

    override fun upload(key: String, input: InputStream, contentType: String?): Boolean {
        val file = resolve(key) ?: return false
        return try {
            file.parentFile?.mkdirs()
            FileOutputStream(file).use { out -> input.copyTo(out) }
            true
        } catch (ex: Exception) {
            Trace.logError("Local file upload failed", ex, mapOf("key" to key))
            false
        }
    }

    /** Strips UUID prefix so browse paths are human-friendly: files/{uuid}/{name}. */
    override fun list(prefix: String): List<String> = try {
        val dir = File(root, "files")
        if (!dir.exists()) return emptyList()
        dir.listFiles()?.flatMap { sub ->
            sub.listFiles()?.map { it.name }?.map { "$prefix/${sub.name}/${it}" } ?: emptyList()
        } ?: emptyList()
    } catch (ex: Exception) {
        Trace.logError("Local file list failed", ex, mapOf("prefix" to prefix))
        emptyList()
    }

    fun resolve(key: String): File? {
        val safe = key.replace("..", "").removePrefix("/")
        val candidate = File(root, safe)
        val canonical = candidate.canonicalPath
        val base = File(root).canonicalPath
        return if (canonical.startsWith(base)) candidate else null
    }

    fun stream(key: String): InputStream? {
        val file = resolve(key) ?: return null
        return if (file.exists()) FileInputStream(file) else null
    }

    companion object {
        fun newKey(): String = "files/${UUID.randomUUID()}"
    }
}