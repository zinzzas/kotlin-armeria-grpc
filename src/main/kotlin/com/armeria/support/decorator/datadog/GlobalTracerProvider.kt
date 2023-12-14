package com.armeria.support.decorator.datadog

import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.RequestContext
import io.opentracing.SpanContext
import io.opentracing.Tracer
import io.opentracing.propagation.Format
import io.opentracing.propagation.TextMapAdapter
import io.opentracing.util.GlobalTracer
import org.springframework.stereotype.Component

@Component
object GlobalTracerProvider {

    fun getTracer(): Tracer {
        return GlobalTracer.get()
    }

    fun determineSpanType(req: HttpRequest): String {
        return if (req.headers()?.contentType().toString().endsWith("grpc")) {
            "grpc.server"
        } else {
            "http.server"
        }
    }

    fun extractParentSpan(tracer: Tracer, ctx: RequestContext): SpanContext? {
        val headers: Map<String, String>? = ctx.request()?.headers()?.associate { it.key.toString() to it.value }
        return tracer.extract(Format.Builtin.HTTP_HEADERS, TextMapAdapter(headers))
    }
}