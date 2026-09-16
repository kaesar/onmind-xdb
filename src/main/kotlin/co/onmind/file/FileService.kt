package co.onmind.file

import java.io.InputStream
import java.time.Duration

/** Metadata row shape shared by S3FileService and FileAPI (XYAny row). */
typealias FileMetadata = MutableMap<String, Any?>

/**
 * Blob storage contract for the `/file` API. Implementations back presigned
 * URLs, object delete and prefix listing. S3FileService targets
 * RustFS/MinIO/AWS S3; FileStorage targets the XDB local folder.
 */
interface FileService : AutoCloseable {

    /** URL to GET an object directly from the bucket (browser download). */
    fun presignGet(key: String, expiresIn: Duration): String

    /** URL to PUT an object directly from the browser (direct upload). */
    fun presignPut(key: String, expiresIn: Duration, contentType: String?): String

    /** URL to DELETE an object directly from the bucket. */
    fun presignDelete(key: String, expiresIn: Duration): String

    /** Server-side delete of the object (fallback when the browser cannot call the presigned URL). */
    fun delete(key: String): Boolean

    /** Server-side upload (fallback path; the primary flow is browser PUT). */
    fun upload(key: String, input: InputStream, contentType: String?): Boolean

    /** List object keys under a prefix. */
    fun list(prefix: String): List<String>

    val bucket: String
    val endpoint: String?
    val region: String

    override fun close() {}
}