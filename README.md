# Testcontainers WireMock Module

WireMock can be used to mock HTTP services by matching requests against user-defined expectations.


## Usage example

The following example shows how to start a WireMockContainer using a JUnit4 Rule.

```java
@Rule
public WireMockContainer wireMockContainer = new WireMockContainer(WIREMOCK_IMAGE)
        .withStubMappingForClasspathResource("stubs/"); // loads all *.json files in resources/stubs/ 
```

You can also start a WireMockContainer by using the `@Testcontainers` and `@Container` annotations when using JUnit5.

```java
@Testcontainers
class ExampleTest {
    @Container
    private static final WireMockContainer WIRE_MOCK_CONTAINER = new WireMockContainer(WIREMOCK_IMAGE)
            .withStubMappingForClasspathResource("stubs/"); // loads all *.json files in resources/stubs/ 
    // ...
}
```

## Adding this module to your project dependencies

Add the following dependency to your `pom.xml/build.gradle file`:

### Maven
```xml
<dependency>
    <groupId>nl.thecheerfuldev</groupId>
    <artifactId>testcontainers-wiremock</artifactId>
    <version>1.16.3</version>
    <scope>test</scope>
</dependency>
```


### Gradle
```groovy
testImplementation "org.testcontainers:mockserver:1.16.3"
```