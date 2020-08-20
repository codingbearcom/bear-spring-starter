/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
plugins {
    id("org.springframework.boot") version "2.3.3.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    id("com.avast.gradle.docker-compose") version "0.13.2"
    id("org.sonarqube") version "2.8"
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
            dependencySet("org.springframework.boot:2.3.3.RELEASE") {
                entry("spring-boot-starter")
                entry("spring-boot-starter-test") {
                    exclude("org.junit.vintage:junit-vintage-engine")
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

            dependency("org.jboss.resteasy:resteasy-spring-boot-starter:4.6.1.Final")

            dependency("javax.inject:javax.inject:1")

            dependency("org.postgresql:postgresql:42.2.15")
            dependency("commons-io:commons-io:2.7")
            dependency("org.apache.commons:commons-collections4:4.4")
            dependency("org.apache.commons:commons-lang3:3.11")

            dependencySet("io.rest-assured:4.2.0") {
                entry("rest-assured")
                entry("xml-path")
                entry("json-path")
            }

            dependency("com.xebialabs.restito:restito:0.9.4") {
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
        toolVersion = "8.35"
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
