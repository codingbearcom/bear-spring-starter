/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation("org.springframework.boot", "spring-boot-starter-web")
    implementation("org.springframework.boot", "spring-boot-starter-web-services")
    implementation("org.springframework.boot", "spring-boot-starter-data-jpa")
    implementation("org.springframework.boot", "spring-boot-starter-jetty")
    implementation("org.springframework.boot", "spring-boot-starter-logging")
    implementation("javax.inject:javax.inject")

    implementation("org.jboss.resteasy:resteasy-spring-boot-starter")

    implementation("org.postgresql", "postgresql")

    implementation("commons-io", "commons-io")
    implementation("org.apache.commons", "commons-collections4")
    implementation("org.apache.commons", "commons-lang3")

    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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
