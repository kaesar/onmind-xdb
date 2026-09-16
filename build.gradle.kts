import org.gradle.jvm.tasks.Jar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.nio.file.Path

val javaVersion = "25"
val kotlinVersion = "2.4.10"
val http4kVersion = "6.57.1.0"
val jacksonVersion = "2.18.3"
val grpcVersion = "1.69.1"
val protobufVersion = "3.25.5"

// Build profile: "full" (default) or "lite" (for GraalVM native image)
// Usage: ./gradlew shadowJar -PbuildProfile=lite
val buildProfile = project.findProperty("buildProfile")?.toString() ?: "full"
val isLite = buildProfile == "lite"

plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
    id("org.graalvm.buildtools.native") version "1.1.8"
    id("gg.jte.gradle") version "3.2.4"
    id("com.google.protobuf") version "0.10.0"
    application
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://repo.maven.apache.org/maven2")
    }
}

dependencies {
    // Core dependencies (always included)
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-server-jetty:$http4kVersion")
    implementation("org.http4k:http4k-format-jackson:$http4kVersion")
    implementation("gg.jte:jte:3.2.4")
    implementation("gg.jte:jte-kotlin:3.2.4")
    implementation("com.h2database:h2:2.4.240")
    implementation("commons-dbutils:commons-dbutils:1.8.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")  // export-to-SQLite (core, lite + full)

    // Optional cloud/DB backends (excluded in lite profile)
    if (!isLite) {
        implementation("software.amazon.awssdk:dynamodb:2.42.17")
        // FILES feature (RustFS/MinIO/AWS S3): the S3Presigner lives inside the
        // s3 artifact itself; aws-crt is optional and excluded (SigV4-only impl).
        implementation("software.amazon.awssdk:s3:2.44.7") {
            exclude(group = "software.amazon.awssdk.crt", module = "aws-crt")
        }
        implementation("com.azure:azure-cosmos:4.78.0")
        implementation("org.rocksdb:rocksdbjni:10.5.1")
        //implementation("org.duckdb:duckdb_jdbc:1.5.0.0")  // ENABLE THIS JUST FOR JAR VERSION
        // DynamoDB pulls netty-nio-client 2.44.7, whose aws-sdk-java-pom parent
        // resolves netty to 4.1.133.Final; the local cache has 4.1.132.Final
        // plus the exact runtime set, so force it to keep offline builds green.
        // (4.1.132 and 4.1.133 are functionally equivalent for these clients.)
        configurations.all {
            resolutionStrategy {
                force(
                    "io.netty:netty-common:4.1.132.Final",
                    "io.netty:netty-buffer:4.1.132.Final",
                    "io.netty:netty-transport:4.1.132.Final",
                    "io.netty:netty-resolver:4.1.132.Final",
                    "io.netty:netty-codec:4.1.132.Final",
                    "io.netty:netty-codec-http:4.1.132.Final",
                    "io.netty:netty-codec-http2:4.1.132.Final",
                    "io.netty:netty-handler:4.1.132.Final",
                    "io.netty:netty-transport-classes-epoll:4.1.132.Final"
                )
            }
        }
    }

    // Optional gRPC transport (excluded in lite profile)
    if (!isLite) {
        implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
        implementation("io.grpc:grpc-protobuf:$grpcVersion")
        implementation("io.grpc:grpc-stub:$grpcVersion")
        implementation("com.google.protobuf:protobuf-java:$protobufVersion")
        compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    }
}

// Protobuf/gRPC compilation (only in full profile)
if (!isLite) {
    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:$protobufVersion"
        }
        plugins {
            create("grpc") {
                artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
            }
        }
        generateProtoTasks {
            all().forEach { task ->
                task.plugins {
                    create("grpc")
                }
            }
        }
    }

    // Ensure Kotlin compilation sees protoc / grpc-java generated sources
    kotlin {
        sourceSets {
            main {
                kotlin.srcDir("build/generated/source/proto/main/java")
                kotlin.srcDir("build/generated/source/proto/main/grpc")
                kotlin.srcDir("src/full/kotlin")
            }
        }
    }
} else {
    // Disable proto tasks in lite profile
    tasks.matching { it.name.contains("proto", ignoreCase = true) }.configureEach {
        enabled = false
    }

    // Stale protobuf generated sources (from a previous full build) must not be compiled
    sourceSets.main {
        java.exclude("**/co/onmind/grpc/proto/**")
    }
    tasks.withType<JavaCompile>().configureEach {
        exclude("**/co/onmind/grpc/proto/**")
    }
}

group = "co.onmind"
version = "1.0.0-early2026"

application {
    mainClass.set("onmindxdb")
}

// Generate build profile property for conditional compilation
val generateBuildConfig = tasks.register("generateBuildConfig") {
    val outputDir = layout.buildDirectory.dir("generated/buildconfig")
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        val content = """
            |build.profile=$buildProfile
            |grpc.enabled=${!isLite}
            |dynamodb.enabled=${!isLite}
            |cosmos.enabled=${!isLite}
        """.trimMargin()
        File(dir, "build.properties").writeText(content)
    }
}

sourceSets.main {
    java.srcDir(layout.buildDirectory.dir("generated/buildconfig"))
}

tasks.named("compileKotlin") {
    dependsOn(generateBuildConfig)
}

/*java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
    }
}*/

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_21.toString()
    targetCompatibility = JavaVersion.VERSION_21.toString()
}

jte {
    generate()
    sourceDirectory.set(Path.of("src", "main", "resources", "kte"))
    contentType.set(gg.jte.ContentType.Html)
}

// Test sources are standalone `main`-based runners (no JUnit @Test methods),
// executed manually. Keep `./gradlew build` green when Gradle finds no tests.
tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}

val jar = tasks.named("jar", Jar::class) {
    manifest {
        attributes["Main-Class"] = "onmindxdb"
    }
}

val shadowJar = tasks.named("shadowJar", ShadowJar::class) {
    archiveClassifier.set(if (isLite) "lite" else "full")
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes(mapOf("Main-Class" to "onmindxdb"))
    }
}

tasks.register("printProfile") {
    doLast {
        println("Build profile: $buildProfile")
        println("Lite mode: $isLite")
        if (isLite) {
            println("Excluded: gRPC/Netty, DynamoDB, Azure Cosmos, RocksDB, DuckDB")
        }
    }
}

tasks.named("assemble").configure {
    dependsOn(shadowJar)  //fatjar
}

/*task dockerBuild(type: Exec) {
    executable "sh"
    args "-c", "docker build -t graal ."
}

dockerBuild.dependsOn(tasks.shadowJar)*/
