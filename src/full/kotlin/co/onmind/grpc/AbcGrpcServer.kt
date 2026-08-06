package co.onmind.grpc

import co.onmind.api.AbcAPI
import co.onmind.util.Trace
import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import java.util.concurrent.TimeUnit

/**
 * Optional Netty-based gRPC server on a dedicated port (default 9991).
 * Lifecycle is independent from the http4k/Jetty HTTP server.
 */
class AbcGrpcServer(
    private val port: Int,
    private val abc: AbcAPI
) {
    private var server: Server? = null

    val isRunning: Boolean get() = server?.isShutdown == false && server != null

    fun start(): AbcGrpcServer {
        if (server != null) return this

        val built = NettyServerBuilder
            .forPort(port)
            .addService(AbcGrpcService(abc, port))
            .maxInboundMessageSize(16 * 1024 * 1024)
            .build()
            .start()

        server = built
        Trace.logInfo("gRPC AbcService listening on port $port (Netty)")
        println("gRPC AbcService => localhost:$port  (Execute, Status)")

        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                stop()
            } catch (_: Exception) {
                // best-effort on JVM exit
            }
        })
        return this
    }

    fun stop() {
        val running = server ?: return
        server = null
        running.shutdown()
        try {
            if (!running.awaitTermination(5, TimeUnit.SECONDS)) {
                running.shutdownNow()
            }
        } catch (_: InterruptedException) {
            running.shutdownNow()
            Thread.currentThread().interrupt()
        }
        Trace.logInfo("gRPC server stopped (port $port)")
    }
}
