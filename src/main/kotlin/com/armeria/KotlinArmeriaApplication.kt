package com.armeria

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(
    // spring data mongodb auto configuration 제외
    exclude = [MongoReactiveAutoConfiguration::class, MongoReactiveDataAutoConfiguration::class]
)
class KotlinArmeriaApplication

fun main(args: Array<String>) {
    runApplication<KotlinArmeriaApplication>(*args)
}
