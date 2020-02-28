package is.stanton.andy.example

import java.util.logging.Logger

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall
import io.grpc.{Metadata, ServerCall, ServerCallHandler, ServerInterceptor}

class HeaderServerInterceptor extends ServerInterceptor {
  private val logger =
    Logger.getLogger(classOf[HeaderServerInterceptor].getName)

  override def interceptCall[ReqT, RespT](
    call: ServerCall[ReqT, RespT],
    headers: Metadata,
    next: ServerCallHandler[ReqT, RespT]
  ): ServerCall.Listener[ReqT] = {
    next.startCall(
      new SimpleForwardingServerCall[ReqT, RespT](call) {
        if (!call.getMethodDescriptor.getFullMethodName
              .startsWith("grpc.reflection")) {
          logger.info(
            s"Intercepted call to ${call.getMethodDescriptor.getFullMethodName} with headers ${headers.toString}"
          )
        }
      },
      headers
    )
  }
}
