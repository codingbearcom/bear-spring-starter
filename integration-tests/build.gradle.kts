/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    testImplementation(project(":application"))
    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.springframework.boot", "spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot", "spring-boot-starter-web")
    testImplementation("io.rest-assured", "rest-assured")
    testImplementation("org.mockito", "mockito-core")
    testImplementation("com.xebialabs.restito", "restito")
    testImplementation("commons-io", "commons-io")
    testImplementation("org.junit.jupiter", "junit-jupiter-api")
    testImplementation("org.junit.jupiter", "junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine")
}

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

configurations["intTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

val composeUp = task("composeUp") {
    group = "Docker"
    description = "Task that delegates to the same task in root project"
    dependsOn(rootProject.tasks["composeUp"])
}

val composeDown = task("composeDown") {
    group = "Docker"
    description = "Task that delegates to the same task in root project"
    dependsOn(rootProject.tasks["composeDown"])
}


val integrationTests = task<Test>("integrationTests") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    shouldRunAfter("test")

    dependsOn(composeUp)
    mustRunAfter(composeUp)

    finalizedBy(composeDown)

}
tasks["test"].mustRunAfter(composeUp)
composeDown.mustRunAfter(integrationTests)

// tasks.check { dependsOn(integrationTests) }

gradle.taskGraph.whenReady {
    tasks.test {
        onlyIf {
            hasTask(integrationTests)
        }
    }
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

