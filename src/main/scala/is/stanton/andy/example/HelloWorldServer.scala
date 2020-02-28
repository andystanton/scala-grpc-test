package is.stanton.andy.example

import java.util.logging.{LogManager, Logger}

import io.grpc._
import io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService

import scala.concurrent.{ExecutionContext, Future}

object HelloWorldServer {
  LogManager.getLogManager.readConfiguration(
    getClass.getClassLoader.getResourceAsStream("logging.properties")
  )
  private val logger = Logger.getLogger(classOf[HelloWorldServer].getName)
  private val port = 50051

  def main(args: Array[String]) {
    val server = new HelloWorldServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
}

class HelloWorldServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = _
  import HelloWorldServer.logger

  private def start() {
    logger.info("Starting server")

    server = NettyServerBuilder
      .forPort(HelloWorldServer.port)
      .intercept(new HeaderServerInterceptor())
      .addService(ExampleGrpc.bindService(new ExampleImpl, executionContext))
      .addService(ProtoReflectionService.newInstance())
      .build
      .start

    logger.info("Server started, listening on " + HelloWorldServer.port)

    sys.addShutdownHook {
      logger.info("Shutting down")
      self.stop()
    }
  }

  private def stop() {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown() {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class ExampleImpl extends ExampleGrpc.Example {
    override def sayHello(req: HelloRequest): Future[HelloReply] = {
      val responseMessage = s"Hello ${req.name}"
      logger.info(s"Received request. Sending response '$responseMessage'")
      Future.successful(HelloReply(message = responseMessage))
    }
  }
}
