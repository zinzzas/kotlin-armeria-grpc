package com.armeria.support.config.armeria

import com.linecorp.armeria.common.HttpHeaderNames
import com.linecorp.armeria.common.HttpHeaders
import com.linecorp.armeria.common.RequestContext
import com.linecorp.armeria.common.grpc.GrpcMeterIdPrefixFunction
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats
import com.linecorp.armeria.common.logging.LogFormatter
import com.linecorp.armeria.common.logging.LogLevel
import com.linecorp.armeria.common.logging.LogWriter
import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.docs.DocServiceFilter
import com.linecorp.armeria.server.grpc.GrpcService
import com.linecorp.armeria.server.logging.AccessLogWriter
import com.linecorp.armeria.server.logging.LoggingService
import com.linecorp.armeria.server.metric.MetricCollectingService
import com.linecorp.armeria.spring.ArmeriaServerConfigurator
import io.grpc.BindableService
import io.grpc.health.v1.HealthGrpc
import io.grpc.protobuf.services.ProtoReflectionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ArmeriaConfiguration(
    private val grpcServices: List<BindableService>,
) {

    @Bean
    fun armeriaServerConfigurator(): ArmeriaServerConfigurator = ArmeriaServerConfigurator {
            configureAccessLog(it)
            configureLogging(it)
            configureGrpcService(it)
            configureDocService(it)
        }

    private fun configureAccessLog(sb: ServerBuilder) =
        sb.accessLogWriter(AccessLogWriter.combined(), true)

    private fun configureLogging(sb: ServerBuilder) {
        val logFormatter = LogFormatter.builderForText()
            .requestHeadersSanitizer { _: RequestContext, headers: HttpHeaders ->
                headers.toBuilder()
                    .removeAndThen(HttpHeaderNames.CONTENT_TYPE)
                    .build().toString()
            }
            .responseHeadersSanitizer { _: RequestContext, headers: HttpHeaders ->
                headers.toBuilder()
                    .add(HttpHeaderNames.CONTENT_TYPE, "application/json")
                    .build().toString()
            }
            .requestContentSanitizer { _: RequestContext, content: Any ->
                val originalContent = content.toString()
                val sanitizedContent = originalContent.replace(Regex("params=\\[([^\\]]*)\\]")) { matchResult ->
                    val params = matchResult.groupValues[1].replace("\n", " ")
                    "params=[$params]"
                }
                sanitizedContent
            }
            .build()

        val logWriter = LogWriter.builder()
            .requestLogLevel(LogLevel.INFO)
            .successfulResponseLogLevel(LogLevel.INFO)
            .failureResponseLogLevel(LogLevel.ERROR)
            .logFormatter(logFormatter)
            .build()

        val loggingDecorator = LoggingService.builder()
            .logWriter(logWriter)
            .newDecorator()
        sb.decorator(loggingDecorator)
    }

    private fun configureDocService(sb: ServerBuilder) {
        val docServiceBuilder = DocService.builder()
            .exclude(DocServiceFilter.ofServiceName(HealthGrpc.SERVICE_NAME))
            //.exampleHeaders(HttpHeaders.of(HttpHeaderNames.AUTHORIZATION,  "Bearer kl12342"))
            .build()
        sb.serviceUnder("/docs", docServiceBuilder)
    }

    private fun configureGrpcService(sb: ServerBuilder) {
        val grpcServiceBuilder = GrpcService.builder()
            .apply {
                grpcServices.forEach { addService(it) }
            }
            .addService(ProtoReflectionService.newInstance())  // gRPC reflection service
            .supportedSerializationFormats(GrpcSerializationFormats.values())
            .enableHealthCheckService(true)
            .enableUnframedRequests(true)
            .enableHttpJsonTranscoding(true)  //HTTP/JSON to gRPC transcoding
            .build()
        sb.service(grpcServiceBuilder)
        sb.decorator(MetricCollectingService.newDecorator(GrpcMeterIdPrefixFunction.of("grpc.service")))
            .build()
    }
}
