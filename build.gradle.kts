import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val javaVersion = "17"
val kotlinVersion = "1.9.22"
val http4kVersion = "5.13.2.0"
val jacksonVersion = "2.16.1"

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "7.1.2"
	id("org.graalvm.buildtools.native") version "0.9.28"
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
    implementation("com.h2database:h2:2.3.232")
    implementation("org.ehcache:ehcache:3.10.8")
    //implementation("org.postgresql:postgresql:42.7.5")
    //implementation("org.duckdb:duckdb_jdbc:1.2.1")  // ENABLE THIS JUST FOR JAR VERSION
    //implementation("org.herddb:herddb-jdbc:0.29.0")  // ENABLE THIS JUST FOR JAR VERSION
    implementation("commons-dbutils:commons-dbutils:1.8.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
}

group = "co.onmind"
version = "1.0.0-final2024"

application {
    mainClass.set("onmindxdb")
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
