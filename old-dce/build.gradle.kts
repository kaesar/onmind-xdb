import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val javaVersion = "11"
val kotlinVersion= "1.7.10"
val jacksonVersion = "2.13.3"

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    //id("maven")
    application
}

repositories {
    mavenCentral()
    maven {
        setUrl("http://repo.maven.apache.org/maven2")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.javalin:javalin:4.6.4")
    implementation("org.eclipse.jetty:jetty-server:9.4.48.v20220622")
    //implementation("org.eclipse.jetty:jetty-server:11.0.11")
    implementation("commons-dbutils:commons-dbutils:1.7")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    //implementation("com.h2database:h2:2.1.214")
    //implementation("monetdb:monetdbe-java:1.10")
    implementation("io.agroal:agroal-pool:1.16")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
}

group = "co.onmind"
version = "1.0.0-graded2022"

application {
    mainClassName = "XDBKt"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = javaVersion

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = javaVersion
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "XDBKt"
    }
}

val shadowJar by tasks.getting(ShadowJar::class) {
    //archiveBaseName.set("onmind-xdb")  //baseName = project.name + "-full"
    archiveClassifier.set("full")
    mergeServiceFiles()
    manifest {
        attributes(mapOf("Main-Class" to "XDBKt"))  //attributes["Main-Class"] = "onmindxdb"
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
