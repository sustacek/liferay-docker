# orca-test

This is a subproject aiming on automatically testing the ORCA tool container in [../orca](../orca).

## Runtime

For now, the only supported runtime is Docker. We build an image based on supported target OS of Orca (Ubuntu Jammy) and execute the Orca scripts in there. See [Dockerfile.orca](Dockerfile.orca).

Since Orca builds its own Docker images (and runs docker-compose eventually), we need Docker server as well. This docker-in-docker problem is solved using the typical approach -- by binding the Docker socket from host (= where Gradle is executed) to the container (= where Orca scripts run). So the Orca scripts run inside the container with Ubuntu, but the Docker images being built willbe created in your machine's Docker server.

## Writing test specs

The testing framework used to define the test cases is [Spock](https://spockframework.org). You can find the specs in the usual path, as they are written in Groovy: [src/test/groovy](src/test/groovy). 

## Running tests (specs) locally
              
Make sure you have Docker up and running.

```shell

$ cd orca-test
$ ./gradlew test
```

Spock is nicely integrated into Gradle, failed / successful tests will be recorded, you'll get reports (HTML) generated (see [build/reports/tests/test/index.html](build/reports/tests/test/index.html) once you execute some tests) etc.