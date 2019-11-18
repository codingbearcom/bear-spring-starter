/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    compile("org.springframework.boot", "spring-boot-starter-web")
    compile("org.springframework.boot", "spring-boot-starter-web-services")
    compile("org.springframework.boot", "spring-boot-starter-data-jpa")
    compile("org.springframework.boot", "spring-boot-starter-jetty")
    compile("org.springframework.boot", "spring-boot-starter-logging")
    compile("javax.inject:javax.inject")

    compile("org.jboss.resteasy:resteasy-spring-boot-starter")

    compile("org.postgresql", "postgresql")

    compile("commons-io", "commons-io")
    compile("org.apache.commons", "commons-collections4")
    compile("org.apache.commons", "commons-lang3")

    testCompile("org.springframework.boot", "spring-boot-starter-test")
    testCompile("org.junit.jupiter:junit-jupiter-api")
    testCompile("org.junit.jupiter:junit-jupiter-params")
    testRuntime("org.junit.jupiter:junit-jupiter-engine")
}

// by default the Spring boot plugin will replace a normal jar with boot jar
// this is a problem if there is another module that depends on this one, because boot jar
// cannot be used as a dependency
tasks.getByName<Jar>("jar") {
    enabled = true
}

tasks.getByName<BootJar>("bootJar") {
    archiveClassifier.set("executable")
    archiveVersion.set("")
    launchScript()
}
