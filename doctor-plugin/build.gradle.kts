import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.3.61"
    id("com.gradle.plugin-publish") version "0.10.1"
    id("org.jmailen.kotlinter") version "2.1.1"
    `maven-publish`
    signing
}

group = "com.osacky.doctor"
version = "0.2.2-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
    implementation("io.reactivex.rxjava3:rxjava:3.0.0-RC8")
    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.12")
    testImplementation("com.google.truth:truth:1.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
}

pluginBundle {
    website = "https://github.com/runningcode/gradle-doctor"
    vcsUrl = "https://github.com/runningcode/gradle-doctor"
    tags = listOf("doctor", "android", "gradle")

    mavenCoordinates {
        artifactId = "doctor-plugin"
        groupId = group
    }
}

gradlePlugin {
    plugins {
        create("doctor-plugin") {
            id = "com.osacky.doctor"
            displayName = "Doctor Plugin"
            description = "The right prescription for your gradle build."
            implementationClass = "com.osacky.doctor.DoctorPlugin"
        }
    }
}


kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

val isReleaseBuild : Boolean = !version.toString().endsWith("SNAPSHOT")

val sonatypeUsername : String? by project
val sonatypePassword : String? by project

publishing {
    repositories {
        repositories {
            maven {
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (isReleaseBuild) releasesRepoUrl else snapshotsRepoUrl
                credentials {
                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom {
                name.set("Gradle Doctor")
                description.set("The right prescription for your Gradle build.")
                url.set("https://github.com/runningcode/gradle-doctor")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("runningcode")
                        name.set("Nelson Osacky")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/runningcode/gradle-doctor.git")
                    developerConnection.set("scm:git:ssh://github.com/runningcode/gradle-doctor.git")
                    url.set("https://github.com/runningcode/gradle-doctor")
                }
            }
        }
    }
}

signing {
    setRequired(isReleaseBuild)
    sign(publishing.publications["mavenJava"])
}

tasks.withType(Test::class.java).configureEach {
    testLogging {
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
    }
}
