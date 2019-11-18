/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    testCompile(project(":application"))
    testCompile("org.springframework.boot", "spring-boot-starter-test")
    testCompile("io.rest-assured", "rest-assured")
    testCompile("org.mockito", "mockito-core")
    testCompile("com.xebialabs.restito", "restito")
    testCompile("commons-io", "commons-io")
    testCompile("org.junit.jupiter", "junit-jupiter-api")
    testCompile("org.junit.jupiter", "junit-jupiter-params")
    testRuntime("org.junit.jupiter", "junit-jupiter-engine")
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

