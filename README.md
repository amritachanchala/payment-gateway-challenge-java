# Instructions for candidates changes by Prashant

This is the Java version of the Payment Gateway challenge. If you haven't already read this [README.md](https://github.com/cko-recruitment/) on the details of this exercise, please do so now.

## Requirements
- JDK 17
- Docker

## Template structure

src/ - A skeleton SpringBoot Application

test/ - Some simple JUnit tests

imposters/ - contains the bank simulator configuration. Don't change this

.editorconfig - don't change this. It ensures a consistent set of rules for submissions when reformatting code

docker-compose.yml - configures the bank simulator


## API Documentation
For documentation openAPI is included, and it can be found under the following url: **http://localhost:8090/swagger-ui/index.html**

**Feel free to change the structure of the solution, use a different library etc.**

## Change added
- A Post API is create which can be accessed at endpoint - POST- http://localhost:8090/payment
- Unit tests are added
- Integration tests are  added which tests different scenarios with POST and GET API 
and are present at "java/com/checkout/payment/gateway/integrationtest/PaymentGatewayIntegrationTest.java"
Pre condition- Docker is running
- Tracing is enables using zipkin and can be accessed at url- http://localhost:9411/zipkin/
- Added below dependencies
  - For validation of request and error messages
    ```gradle
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    ```
  - For Zipkin tracing
    ```gradle
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-tracing-bridge-brave'
    implementation 'io.zipkin.reporter2:zipkin-reporter-brave'
    ```
