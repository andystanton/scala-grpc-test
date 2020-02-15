package is.stanton.andy.example

import java.util.logging.{LogManager, Logger}

import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.{Server, ServerBuilder}

import scala.concurrent.{ExecutionContext, Future}

object HelloWorldServer {
  LogManager.getLogManager.readConfiguration(
    getClass.getClassLoader.getResourceAsStream("logging.properties")
  )
  private val logger = Logger.getLogger(classOf[HelloWorldServer].getName)
  private val port = 50051

  def main(args: Array[String]) {
    logger.info("Starting server")
    val server = new HelloWorldServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
}

class HelloWorldServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = _

  private def start() {
    server = ServerBuilder
      .forPort(HelloWorldServer.port)
      .addService(ExampleGrpc.bindService(new ExampleImpl, executionContext))
      .addService(ProtoReflectionService.newInstance())
      .build
      .start

    HelloWorldServer.logger.info(
      "Server started, listening on " + HelloWorldServer.port
    )
    sys.addShutdownHook {
      HelloWorldServer.logger.info("Shutting down")
      self.stop()
      HelloWorldServer.logger.info("gRPC server shut down")
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
      HelloWorldServer.logger.info(
        s"Received request. Sending response '$responseMessage'"
      )
      Future.successful(HelloReply(message = responseMessage))
    }
  }
}
