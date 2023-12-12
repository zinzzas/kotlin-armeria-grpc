package com.armeria.support.error

import com.linecorp.armeria.common.RequestContext
import com.linecorp.armeria.common.grpc.GrpcExceptionHandlerFunction
import com.linecorp.armeria.internal.common.grpc.GrpcStatus
import io.grpc.Metadata
import io.grpc.Status
import org.springframework.stereotype.Component

@Component
class GlobalGrpcExceptionHandler: GrpcExceptionHandlerFunction {
    /**
     * Maps the specified [Throwable] to a gRPC [Status],
     * and mutates the specified [Metadata].
     * If `null` is returned, the built-in mapping rule is used by default.
     */
    override fun apply(ctx: RequestContext, cause: Throwable, metadata: Metadata): Status {


        return GrpcStatus.fromThrowable(cause)
    }
}