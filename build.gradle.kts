/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
plugins {
    id("org.springframework.boot") version "2.1.1.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("com.avast.gradle.docker-compose") version "0.8.12"
    id ("org.sonarqube") version "2.8"
    checkstyle
    java
}

group = "com.codingbear"
version = "1.0"

dockerCompose {
    useComposeFiles = listOf("config/docker/environment-compose.yml")
}

allprojects {
    group = "com.codingbear"
    version = "$version"
}

subprojects {
    plugins.apply("io.spring.dependency-management")
    plugins.apply("org.springframework.boot")
    plugins.apply("java")
    plugins.apply("checkstyle")

    dependencyManagement {
        dependencies {
            dependencySet("org.junit.jupiter:5.3.2") {
                entry("junit-jupiter-api")
                entry("junit-jupiter-engine")
                entry("junit-jupiter-params")
            }

            dependencySet("org.springframework.boot:2.1.1.RELEASE") {
                entry("spring-boot-starter")
                entry("spring-boot-starter-test") {
                    // the test starter comes with junit 4, we will replace it with junit 5
                    exclude("junit:junit")
                }

                entry("spring-boot-starter-jetty")
                entry("spring-boot-starter-web") {
                    exclude("org.springframework.boot:spring-boot-starter-tomcat")
                    exclude("org.springframework:spring-webmvc")
                }
                entry("spring-boot-starter-data-jpa")
                entry("spring-boot-starter-logging")
                entry("spring-boot-starter-jdbc")
            }

            // resteasy spring boot starter has also dependency on resteasy; both should be in sync
            dependencySet("org.jboss.resteasy:3.8.0.Final") {
                entry("resteasy-jaxrs")
                entry("resteasy-json-p-provider'")
                entry("resteasy-client")
                entry("resteasy-jackson2-provider")
            }

            dependency("org.jboss.resteasy:resteasy-spring-boot-starter:3.1.0.Final")

            dependency("javax.inject:javax.inject:1")

            dependency("org.postgresql:postgresql:42.2.5")
            dependency("commons-io:commons-io:2.6")
            dependency("org.apache.commons:commons-collections4:4.3")
            dependency("org.apache.commons:commons-lang3:3.9")
            dependency("io.rest-assured:rest-assured:3.1.1")

            dependency("com.xebialabs.restito:restito:0.9.3") {
                // this comes with junit 4, we will replace it with junit 5
                exclude("junit:junit")
            }

        }
    }

    repositories {
        jcenter()
        mavenCentral()
    }

    checkstyle {
        toolVersion = "8.1"
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    //disable checkstyle in tests
    tasks.checkstyleTest {
        enabled = false
    }

    tasks.test {
        useJUnitPlatform()
//    forkEvery = 20

        testLogging {
            showStandardStreams = true
        }
    }
}
