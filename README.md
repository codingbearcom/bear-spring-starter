![Coding Bear logo](coding-bear/logo.png)

# Bear Spring Starter

## What is it?
**Bear Spring Starter** is a no-nonsense Spring project skeleton that includes all basic libraries and configurations
that Coding Bear programmers find useful for quick spin-up of a new project.

## Why are we sharing this?
Setting up new project can be a lengthy and painful process with lot of trial and error, 
that can take from several hours up to several working days, depending on your experience-level and on how often you face 
such a situation. 

We at Coding Bear felt the need for a go-to skeleton, that we could use each time we start working on something new and 
that would save us from doing repetitive work. With our starter skeleton, we can (almost) skip the set-up phase, 
spend our time in a more efficient manner and save our clients some money.

And because we love, use and benefit from open source, now when we have this opportunity, it feels like a right time to give 
something back.

## How should you use it?
At your own risk :-) Browse the code, adjust it to your taste, build something useful on top of it. 

## Is it under active development?
Depends, rather occasionally. We want to keep the skeleton reasonably up to date (i.e. not necessarily always the latest version of all the libraries, 
but the ones we are currently using). We also plan to add more features later on, but at the same time, the project should not get overly complex.
We described this as a "no-nonsense" skeleton and we'd like to keep it as such.   
Thus, we might create a feature-richer version in a separate repository in the future or create a separate branch. 

## Can I help?
Definitely, feel free to open issues and pull requests. Don't hesitate to comment, fork, contact us and give feedback.

# Quick feature overview
The tech stack is recent while still being slightly conservative and time-proven. It reflects how we think main-stream modern 
applications are (or should be) built today.

## Technical features
* Gradle wrapper, Kotlin based Gradle build scripts
* Spring Boot
* dependency injection
* Spring Data, JPA, PostgreSQL
* REST API (JAX-RS / RestEasy)
* JUnit5, integration tests using Docker, Mockito
* custom-built .gitignore
* sample Sonar integration

## Sample use case
The project also contains one sample end-to-end scenario. 

* fully functional sample "user" REST endpoint, service and repository
* prepared connection to PostgreSQL database through Spring JPA
* sample unit test and integration test

# Usage

## Development database

Project includes Gradle plugin that allows simple spin-ups of docker-compose containing PostgreSQL instance by running Gradle task.

To change container settings, simply edit YAML config file in `/config/docker/environment-compose.yml`. These changes should then be propagated to Hibernate configuration in all applications depending on this database (namely `application.yml` config files in resource folder of each runnable project).

All configuration happens in `/config/docker/environment-compose.yml` file. It obeys [standard compose file structure](https://docs.docker.com/compose/compose-file/).

Hook up of Gradle plugin happens in `/build.gradle.kts` script.

## Running the project

### Build
* To perform only common build, run `./gradlew clean build`
* To build and run tests, run `./gradlew clean build integrationTests`
* To create an executable jar file `./gradlew bootJar` (it should be created in `/application/build/libs`)

### Develpoment PostgreSQL database

```bash
./gradlew composeUp
```

This will start a PostgreSQL DB instance running in the docker container and applies the `application/src/main/resources/sql/schema.sql` script.

You can use e.g. `psql -h localhost -U postgres -d bss` to connect to the DB and browse the structure.

### Sample project

Sample project depends on having database running, so remember to spin it up (project includes option to run instance in Docker container included as Gradle task in root build script - see [Running PostgreSQL database](#postgresql-database)).

Application can be started in multiple ways. 

* use your favourite IDE to run it in a standard or debug mode.
* type `./gradlew bootRun` in terminal
* use `java -jar <executable.jar>` (executable jar file has to be created first)

### Sample Sonar integration

Edit `gradle.properties` and set your Sonar instance URI and authentication token, then run `./gradlew sonarqube` and check the result of the 
analysis in your Sonar's interface.

# Licence
This software is licenced under MIT Licence.

# About Coding Bear

* We are Prague-based software company with passion for doing things better. We love to deliver clean and efficient code.   
* See more of what we do and what we can do for you at our [website](https://codingbear.com).
