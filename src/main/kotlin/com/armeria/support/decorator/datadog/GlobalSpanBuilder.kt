package com.armeria.support.decorator.datadog

import com.linecorp.armeria.common.RequestContext
import datadog.trace.api.DDTags
import io.opentracing.Span
import io.opentracing.SpanContext
import io.opentracing.Tracer
import io.opentracing.tag.Tags
import org.springframework.stereotype.Component

@Component
object GlobalSpanBuilder {

    fun buildSpan(tracer: Tracer, spanType: String, parentSpan: SpanContext?, ctx: RequestContext): Span {
        return tracer
            .buildSpan(spanType)
            .asChildOf(parentSpan)
            .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER)
            .withTag(DDTags.SERVICE_NAME, "armeria-service")
            .withTag(DDTags.RESOURCE_NAME, ctx.method().name + ' ' + ctx.path())
            .withTag(DDTags.HTTP_QUERY, ctx.query())
            .withTag(Tags.HTTP_METHOD, ctx.method().name)
            .withTag(Tags.HTTP_URL, ctx.uri().toString())
            .start()
    }
}