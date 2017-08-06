# Kontentsu CMS

Kontentsu is a different take on a content management system. Unlike many traditional content management systems, that renders content on the server, Kontentsu instead provides a modern REST API for accessing and managing content.

Kontentsu is headless and decoupled and can can publish content to HTTP server like Apache.

## Status

Kontentsu is currently under development.

## Building

The Kontentsu is build using Gradle.

To build the application run the following command:

```
gradle build
```

## Running

Kontentsu is configured to run using Payara Micro - an embedded
release of the Payara Java EE 7 container.

To run Kontentsu using Gradle use the following command (default port is 9090):

```
gradle :api:startPayara
```

To run from command line use and start the Kontentsu on port 8080:

```
java -jar payara-micro-4.1.2.172.jar --port 8080 --deploy kontentsu.war --deploy oauth.war
```

Kontentsu can then be accessed using http://localhost:8080/kontentsu/.

The REST API is documented using [Swagger](http://swagger.io/) and the documentation
can be found using root context of the application.

Payara Micro can be downloaded form http://www.payara.fish/downloads.
