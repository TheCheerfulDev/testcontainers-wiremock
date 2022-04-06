package nl.thecheerfuldev.testcontainers.wiremock;

import io.restassured.RestAssured;
import org.junit.Test;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class WireMockContainerTest {

    private static final DockerImageName WIREMOCK_IMAGE = DockerImageName.parse("wiremock/wiremock").withTag("2.32.0");

    @Test
    public void providingStubsViaDirectoryMappingReturnsCorrectResult() {
        try (WireMockContainer wireMockContainer = new WireMockContainer(WIREMOCK_IMAGE)) {
            wireMockContainer.withStubMappingForClasspathResource("stubs/");
            wireMockContainer.start();

            RestAssured.port = wireMockContainer.getHttpPort();
            assertResponse();
        }
    }

    @Test
    public void providingStubsViaFileMappingReturnsCorrectResult() {
        try (WireMockContainer wireMockContainer = new WireMockContainer(WIREMOCK_IMAGE)) {
            wireMockContainer.withStubMappingForClasspathResource("stubs/api_test_stub.json");
            wireMockContainer.start();

            RestAssured.port = wireMockContainer.getHttpPort();
            assertResponse();
        }
    }

    @Test
    public void providingStubsViaStringMappingReturnsCorrectResult() {
        try (WireMockContainer wireMockContainer = new WireMockContainer(WIREMOCK_IMAGE)) {

            wireMockContainer.withStubMappingForString("""
                    {
                      "request": {
                        "urlPath": "/api/test"
                      },
                      "response": {
                        "status": 200,
                        "body": "Content for /api/test",
                        "headers": {
                          "Content-Type": "text/plain"
                        }
                      }
                    }
                    """);
            wireMockContainer.start();

            RestAssured.port = wireMockContainer.getHttpPort();
            assertResponse();
        }
    }

    private void assertResponse() {
        String responseBody = given().when()
                .get(URI.create("/api/test"))
                .then()
                .statusCode(200).contentType("text/plain")
                .extract().body().asString();

        assertThat(responseBody).isEqualTo("Content for /api/test");
    }
}
