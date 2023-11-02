# Employee Management Service

## Overview

Employee Management Service is a Spring Boot application that provides a RESTful API for managing employee data. This service acts as a Kafka producer, publishing messages when employee records are created, updated, or deleted.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

## Running
To build the project, open your terminal and navigate to the project's root directory. Execute the following Gradle command to compile the application and create an executable JAR file:

```shell
./gradlew build
````
Once the build completes successfully, you can start the application with the following command:
````shell
./gradlew bootRun
````
## Swagger API Documentation
After starting the service, you can access the Swagger UI to interact with the API at:

http://localhost:8080/swagger-ui.html

This documentation provides an interactive way to explore the available endpoints, their required parameters, and the structure of expected request and response bodies.
## Authentication

The service is protected with basic authentication for create, update, and delete operations. It utilizes an in-memory authentication mechanism. The credentials are as follows:

- **Username:** admin
- **Password:** admin

Please ensure to change these credentials when moving to a production environment or consider implementing a more robust authentication mechanism.

## Kafka Integration
The Employee Management Service publishes messages to a Kafka topic as part of its functionality. To integrate Kafka:

1. Download Kafka from the [Apache Kafka website](https://kafka.apache.org/downloads). Follow the Quickstart guide, which can be found [here](https://kafka.apache.org/quickstart), to install and start Kafka services.
2. Change your current directory to the root of the Kafka directory that was downloaded in the previous step.
   ```shell
   cd path/to/kafka
   ```
3. Start the ZooKeeper server using the following command:
   ```shell
   bin/zookeeper-server-start.sh config/zookeeper.properties
   ```
4. In a new terminal window or tab, start the Kafka server with the command:
   ```shell
   bin/kafka-server-start.sh config/server.properties
   ```
5. Start a Kafka consumer to listen to the topic that the service publishes messages to by running:
   ```shell
   bin/kafka-console-consumer.sh --topic employee-events --from-beginning --bootstrap-server localhost:9092
   ```
   Make sure the Kafka and ZooKeeper services are running before starting the consumer.
