import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.*

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.google.protobuf") version "0.9.2"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    kotlin("kapt") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
}

group = "com.armeria"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

object Version {
    const val grpc = "1.58.0"
    const val grpcKotlin = "1.4.0"
    const val protoc = "3.24.0"
    const val kotest = "5.5.5"
    const val armeria = "1.26.3"
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    // Armeria
    implementation(platform("com.linecorp.armeria:armeria-bom:${Version.armeria}"))
    implementation("com.linecorp.armeria:armeria-grpc")
    implementation("com.linecorp.armeria:armeria-kotlin")
    implementation("com.linecorp.armeria:armeria-spring-boot3-autoconfigure")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // gRPC
    implementation("io.grpc:grpc-kotlin-stub:${Version.grpcKotlin}")
    implementation("com.google.protobuf:protobuf-kotlin:${Version.protoc}")

    // datadog tracer
    implementation("com.datadoghq:dd-trace-api:1.21.0")
    implementation("io.opentracing:opentracing-api:0.33.0")
    implementation("io.opentracing:opentracing-util:0.33.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

plugins.withType<ProtobufPlugin> {
    sourceSets {
        main {
            proto {
                srcDir("/proto/order")
            }
        }
    }

    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:${Version.protoc}"
        }

        plugins {
            id("grpc") {
                artifact = "io.grpc:protoc-gen-grpc-java:${Version.grpc}"
            }
            id("grpckt") {
                artifact = "io.grpc:protoc-gen-grpc-kotlin:${Version.grpcKotlin}:jdk8@jar"
            }
        }

        generateProtoTasks {
            ofSourceSet("main").forEach{
                it.plugins {
                    id("grpc")
                    id("grpckt")
                }
                it.builtins {
                    id("kotlin")
                }
            }
        }
    }
}
