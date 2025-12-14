import org.gradle.jvm.tasks.Jar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.nio.file.Path

val javaVersion = "17"
val kotlinVersion = "2.0.21"
val http4kVersion = "5.47.0.0"
val jacksonVersion = "2.18.3"

plugins {
    kotlin("jvm") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.graalvm.buildtools.native") version "0.10.4"
    id("gg.jte.gradle") version "3.1.15"
    application
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://repo.maven.apache.org/maven2")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-format-jackson:$http4kVersion")
    implementation("org.http4k:http4k-contract:$http4kVersion")
    implementation("org.http4k:http4k-metrics-micrometer:$http4kVersion")
    implementation("gg.jte:jte:3.1.15")
    implementation("gg.jte:jte-kotlin:3.1.15")
    implementation("com.h2database:h2:2.3.232")
    implementation("org.ehcache:ehcache:3.10.8")
    implementation("software.amazon.awssdk:dynamodb:2.29.39")
    //implementation("org.postgresql:postgresql:42.7.5")
    //implementation("org.duckdb:duckdb_jdbc:1.2.1")  // ENABLE THIS JUST FOR JAR VERSION
    //implementation("org.herddb:herddb-jdbc:0.29.0")  // ENABLE THIS JUST FOR JAR VERSION
    implementation("commons-dbutils:commons-dbutils:1.8.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("io.agroal:agroal-pool:2.5")
}

group = "co.onmind"
version = "1.0.0-final2025"

application {
    mainClass.set("onmindxdb")
}

/*java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
    }
}*/

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

jte {
    generate()
    sourceDirectory.set(Path.of("src", "main", "resources", "kte"))
    contentType.set(gg.jte.ContentType.Html)
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "onmindxdb"
    }
}

val shadowJar by tasks.getting(ShadowJar::class) {
    //archiveBaseName.set("onmind-xdb-full")  //baseName = project.name + "-full"
    archiveClassifier.set("full")
    mergeServiceFiles()
    manifest {
        attributes(mapOf("Main-Class" to "onmindxdb"))  //attributes["Main-Class"] = "onmindxdb"
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
