package com.armeria.support.decorator.error

import com.armeria.support.decorator.datadog.GlobalTracerProvider
import com.linecorp.armeria.common.RequestContext
import com.linecorp.armeria.common.grpc.GrpcExceptionHandlerFunction
import com.linecorp.armeria.internal.common.grpc.GrpcStatus
import datadog.trace.api.DDTags
import io.grpc.Metadata
import io.grpc.Status
import io.opentracing.tag.Tags
import org.springframework.stereotype.Component

@Component
class GlobalGrpcExceptionHandler(
    private val globalTracerProvider: GlobalTracerProvider,
): GrpcExceptionHandlerFunction {
    constructor(): this(GlobalTracerProvider)

    override fun apply(ctx: RequestContext, cause: Throwable, metadata: Metadata): Status {
        val activeSpan = globalTracerProvider.getTracer().activeSpan()

        if (activeSpan != null) {
            with(activeSpan) {
                setTag(Tags.ERROR, true)
                setTag(DDTags.ERROR_MSG, cause.message)
                setTag(DDTags.ERROR_TYPE, cause.javaClass.name)
                setTag(DDTags.ERROR_STACK, cause.stackTraceToString())
            }
            activeSpan.finish()
        }
        return GrpcStatus.fromThrowable(cause)
    }
}