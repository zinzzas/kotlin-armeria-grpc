package com.armeria.support.decorator.datadog

import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.server.DecoratingHttpServiceFunction
import com.linecorp.armeria.server.HttpService
import com.linecorp.armeria.server.ServiceRequestContext
import datadog.trace.api.DDTags
import io.opentracing.tag.Tags
import org.springframework.stereotype.Component

@Component
class DatadogTracerService(
    private val globalSpanBuilder: GlobalSpanBuilder,
    private val globalTracerProvider: GlobalTracerProvider
): DecoratingHttpServiceFunction {
    constructor(): this(GlobalSpanBuilder, GlobalTracerProvider)

    override fun serve(delegate: HttpService, ctx: ServiceRequestContext, req: HttpRequest): HttpResponse {
        val tracer = globalTracerProvider.getTracer()
        val spanType = globalTracerProvider.determineSpanType(req)
        val parentSpan = globalTracerProvider.extractParentSpan(tracer, ctx)

        val span = globalSpanBuilder.buildSpan(tracer, spanType, parentSpan, ctx)

        try {
            tracer.activateSpan(span).use {
                return delegate.serve(ctx, req)
            }
        } catch (e: Exception) {
            span.setTag(Tags.ERROR, true)
            span.setTag(DDTags.ERROR_MSG, e.message)
            span.setTag(DDTags.ERROR_TYPE, e.javaClass.name)
            span.setTag(DDTags.ERROR_STACK, e.stackTraceToString())
            throw e
        } finally {
            span.finish()
        }
    }
}