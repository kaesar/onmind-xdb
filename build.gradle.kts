import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val javaVersion = "17"
val kotlinVersion = "1.9.22"
val http4kVersion = "5.13.2.0"
val jacksonVersion = "2.16.1"
val sl4jVersion = "1.7.36"

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "7.1.2"
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
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-format-jackson:$http4kVersion")
    //implementation("org.http4k:http4k-server-netty:$http4kVersion")
    //implementation("org.http4k:http4k-aws:$http4kVersion")
    //implementation("org.http4k:http4k-cloudnative:$http4kVersion")
    //implementation("org.http4k:http4k-multipart:$http4kVersion")
    //implementation("org.http4k:http4k-template-handlebars:$http4kVersion")
    //implementation("org.http4k:http4k-client-apache:$http4kVersion")
    //implementation("org.duckdb:duckdb_jdbc:0.6.1")
    //implementation("org.xerial:sqlite-jdbc:3.45.0.0")
    implementation("com.h2database:h2:2.2.224")
    implementation("commons-dbutils:commons-dbutils:1.8.1")
    implementation("org.slf4j:slf4j-simple:$sl4jVersion")
    implementation("org.slf4j:slf4j-api:$sl4jVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    //implementation("org.jetbrains.exposed:exposed-core:0.24.1")
    //implementation("org.jetbrains.exposed:exposed-dao:0.24.1")
    //implementation("org.jetbrains.exposed:exposed-jdbc:0.24.1")
}

group = "co.onmind"
version = "1.0.0-final2024"

application {
    mainClassName = "onmindxdb"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = javaVersion
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = javaVersion
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
