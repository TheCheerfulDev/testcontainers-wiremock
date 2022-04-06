package nl.thecheerfuldev.testcontainers.wiremock;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;

import static io.restassured.RestAssured.given;

public class WireMockContainerRuleTest {

    private static final DockerImageName WIREMOCK_IMAGE = DockerImageName.parse("wiremock/wiremock").withTag("2.32.0");

    @Rule
    public WireMockContainer wireMockContainer = new WireMockContainer(WIREMOCK_IMAGE)
            .withStubMappingForClasspathResource("stubs/api_test_stub.json");

    @Before
    public void setUp() {
        RestAssured.port = wireMockContainer.getHttpPort();
    }

    @Test
    public void providingStubsViaFileMappingReturnsCorrectResult() {
        given().when()
                .get(URI.create("/api/test"))
                .then()
                .statusCode(200).contentType("text/plain");
    }
}
