package co.onmind.file

import co.onmind.util.Trace
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.io.InputStream
import java.net.URI
import java.time.Duration

/**
 * S3-compatible blob backend (RustFS, MinIO, AWS S3, Cloudflare R2...).
 *
 * Presigning uses the AWS SDK v2 S3Presigner (SigV4, no CRT). For custom
 * endpoints (RustFS/MinIO) path-style access is enabled, matching how most
 * testbeds expose buckets.
 */
class S3FileService(
    override val bucket: String,
    override val endpoint: String? = null,
    override val region: String = "us-east-1",
    accessKey: String? = null,
    secretKey: String? = null
) : FileService {

    private val presigner: S3Presigner = run {
        val builder = S3Presigner.builder()
            .region(Region.of(region))
            .serviceConfiguration(
                S3Configuration.builder().pathStyleAccessEnabled(true).build()
            )
        val ep = endpoint
        if (!ep.isNullOrBlank()) builder.endpointOverride(URI.create(ep))
        if (!accessKey.isNullOrBlank() && !secretKey.isNullOrBlank()) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
            )
        }
        builder.build()
    }

    private val client: S3Client by lazy {
        val builder = S3Client.builder()
            .region(Region.of(region))
            .serviceConfiguration(
                S3Configuration.builder().pathStyleAccessEnabled(true).build()
            )
        val ep = endpoint
        if (!ep.isNullOrBlank()) builder.endpointOverride(URI.create(ep))
        if (!accessKey.isNullOrBlank() && !secretKey.isNullOrBlank()) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
            )
        }
        builder.build()
    }

    override fun presignGet(key: String, expiresIn: Duration): String {
        val request = GetObjectRequest.builder().bucket(bucket).key(key).build()
        val presign = GetObjectPresignRequest.builder()
            .getObjectRequest(request)
            .signatureDuration(expiresIn)
            .build()
        return presigner.presignGetObject(presign).url().toString()
    }

    override fun presignPut(key: String, expiresIn: Duration, contentType: String?): String {
        val putBuilder = PutObjectRequest.builder().bucket(bucket).key(key)
        if (!contentType.isNullOrBlank()) putBuilder.contentType(contentType)
        val presign = PutObjectPresignRequest.builder()
            .putObjectRequest(putBuilder.build())
            .signatureDuration(expiresIn)
            .build()
        return presigner.presignPutObject(presign).url().toString()
    }

    override fun presignDelete(key: String, expiresIn: Duration): String {
        val request = DeleteObjectRequest.builder().bucket(bucket).key(key).build()
        val presign = DeleteObjectPresignRequest.builder()
            .deleteObjectRequest(request)
            .signatureDuration(expiresIn)
            .build()
        return presigner.presignDeleteObject(presign).url().toString()
    }

    override fun delete(key: String): Boolean = try {
        client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build())
        true
    } catch (ex: Exception) {
        Trace.logError("S3 delete failed", ex, mapOf("bucket" to bucket, "key" to key))
        false
    }

    override fun upload(key: String, input: InputStream, contentType: String?): Boolean = try {
        val putBuilder = PutObjectRequest.builder().bucket(bucket).key(key)
        if (!contentType.isNullOrBlank()) putBuilder.contentType(contentType)
        client.putObject(putBuilder.build(), software.amazon.awssdk.core.sync.RequestBody.fromInputStream(input, -1))
        true
    } catch (ex: Exception) {
        Trace.logError("S3 upload failed", ex, mapOf("bucket" to bucket, "key" to key))
        false
    }

    override fun list(prefix: String): List<String> = try {
        val request = ListObjectsV2Request.builder()
            .bucket(bucket)
            .prefix(prefix)
            .build()
        val response = client.listObjectsV2(request)
        response.contents().mapNotNull { it.key() }
    } catch (ex: Exception) {
        Trace.logError("S3 list failed", ex, mapOf("bucket" to bucket, "prefix" to prefix))
        emptyList()
    }

    override fun close() {
        try { presigner.close() } catch (_: Exception) {}
        try { client.close() } catch (_: Exception) {}
    }
}