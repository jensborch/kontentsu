# Kontentsu CDN Publishing Service

## Building

The CDN Publishing Service is build using Gradle.

To build the application run the following command:

```
gradle build
```

## Running

The CDN Publishing Service is configured to run using Payara Micro - an embedded
release of the Payara Java EE 7 container.

To run CDN server using Gradle use the following command (default port is 9090):

```
gradle :api:startPayara
```

To run from command line use and start the CDN on port 8080:

```
"c:\Program Files\Java\jdk1.8.0_60\bin\java" -jar payara-micro-4.1.1.162.jar --port 8080 --deploy cdn.war
```

The CDN can then be accessed using http://localhost:8080/cdn/.

The REST API is documented using [Swagger](http://swagger.io/) and the documentation
can be found using root context of the application.

Payara Micro can be downloaded form http://www.payara.fish/downloads.
