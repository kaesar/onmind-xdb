package co.onmind.grpc

import co.onmind.api.AbcAPI
import co.onmind.grpc.proto.AbcRequest
import co.onmind.grpc.proto.AbcResponse
import co.onmind.grpc.proto.AbcServiceGrpc
import co.onmind.grpc.proto.StatusRequest
import co.onmind.grpc.proto.StatusResponse
import co.onmind.io.AbcBody
import co.onmind.util.JsonMapper
import co.onmind.util.Rote
import co.onmind.util.Trace
import com.fasterxml.jackson.databind.JsonNode
import io.grpc.stub.StreamObserver
import org.http4k.core.Status

/**
 * gRPC adapter over the shared ABC dispatcher ([AbcAPI.dispatch]).
 * Does not touch H2/KV directly — same core path as HTTP `/abc` and MCP.
 */
class AbcGrpcService(
    private val abc: AbcAPI,
    private val grpcPort: Int
) : AbcServiceGrpc.AbcServiceImplBase() {

    private val mapper = JsonMapper.instance

    override fun execute(request: AbcRequest, responseObserver: StreamObserver<AbcResponse>) {
        try {
            val body = toAbcBody(request)
            val authUser = request.user.takeIf { it.isNotBlank() } ?: "grpc"
            val httpResponse = abc.dispatch(body, authUser)
            responseObserver.onNext(toAbcResponse(httpResponse.bodyString(), httpResponse.status))
            responseObserver.onCompleted()
        } catch (ex: Exception) {
            Trace.logError("gRPC Execute failed: ${ex.message}")
            responseObserver.onNext(
                AbcResponse.newBuilder()
                    .setOk(false)
                    .setStatus(Status.INTERNAL_SERVER_ERROR.code.toString())
                    .setMessage(ex.message ?: "Internal server error")
                    .setTotal(0)
                    .build()
            )
            responseObserver.onCompleted()
        }
    }

    override fun status(request: StatusRequest, responseObserver: StreamObserver<StatusResponse>) {
        val reply = StatusResponse.newBuilder()
            .setOk(true)
            .setStatus("200")
            .setService("OnMind-XDB")
            .setVersion(onmindxdb.version)
            .setDriver(onmindxdb.driver)
            .setEmbedded(Rote.embedded)
            .setGrpcPort(grpcPort)
            .build()
        responseObserver.onNext(reply)
        responseObserver.onCompleted()
    }

    private fun toAbcBody(req: AbcRequest): AbcBody {
        fun String.orNull(): String? = if (isBlank()) null else this

        return AbcBody(
            way = req.way.ifBlank { "sql" },
            what = req.what.ifBlank { "!" },
            from = req.from.ifBlank { "xyany" },
            some = req.some.orNull(),
            with = req.with.orNull(),
            show = req.show.orNull(),
            how = req.how.orNull(),
            puts = req.puts.orNull(),
            cast = req.cast.orNull(),
            size = req.size.ifBlank { "1200" },
            call = req.call.orNull(),
            keys = req.keys.orNull(),
            user = req.user.orNull(),
            auth = req.auth.orNull(),
            pin = req.pin.orNull(),
            hint = req.hint.orNull(),
            icon = req.icon.orNull(),
            level = req.level.orNull()
        )
    }

    private fun toAbcResponse(bodyJson: String, httpStatus: Status): AbcResponse {
        val builder = AbcResponse.newBuilder()
            .setStatus(httpStatus.code.toString())

        if (bodyJson.isBlank()) {
            return builder
                .setOk(httpStatus.successful)
                .setMessage(if (httpStatus.successful) "" else "Empty response")
                .build()
        }

        return try {
            val root: JsonNode = mapper.readTree(bodyJson)
            val ok = root.path("ok").asBoolean(httpStatus.successful)
            builder.setOk(ok)

            if (root.hasNonNull("status")) {
                builder.setStatus(root.get("status").asText(httpStatus.code.toString()))
            }

            if (root.has("message") && !root.get("message").isNull) {
                builder.setMessage(root.get("message").asText(""))
            }

            if (root.has("total") && root.get("total").isNumber) {
                builder.setTotal(root.get("total").asInt(0))
            }

            if (root.has("data") && !root.get("data").isNull) {
                builder.setDataJson(mapper.writeValueAsString(root.get("data")))
                if (!root.has("total")) {
                    val data = root.get("data")
                    if (data.isArray) builder.setTotal(data.size())
                }
            } else if (ok && root.has("message")) {
                // sendSuccess(String) path: message-only payload
            }

            // Plain success maps (e.g. whoami / status) put fields at root without
            // classic AbcBack data/message shape. Preserve the whole payload.
            if (!root.has("data") && !root.has("message") && root.fieldNames().asSequence().any { it != "ok" && it != "status" && it != "total" }) {
                builder.setDataJson(bodyJson)
            }

            builder.build()
        } catch (_: Exception) {
            builder
                .setOk(httpStatus.successful)
                .setMessage(bodyJson)
                .build()
        }
    }
}
