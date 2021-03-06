package nl.thecheerfuldev.testcontainers.wiremock;

import lombok.SneakyThrows;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WireMockContainer extends GenericContainer<WireMockContainer> {

    public static final String IMAGE = "wiremock/wiremock";
    public static final String DEFAULT_TAG = "2.32.0";
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse(IMAGE);
    private static final String MAPPING_DIR = "/home/wiremock/mappings/";
    public static final Integer WIREMOCK_HTTP_PORT = 8080;
    public static final Integer WIREMOCK_HTTPS_PORT = 8443;

    private final Set<String> stubsFromClasspath = new HashSet<>();
    private final List<String> stubsFromTextBlock = new ArrayList<>();

    /**
     * @since 1.16.3
     * @deprecated use {@link #WireMockContainer(DockerImageName)} or {@link #WireMockContainer(String)} instead.
     */
    @Deprecated(since = "1.16.3")
    public WireMockContainer() {
        this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
    }

    public WireMockContainer(final String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public WireMockContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);

        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        this.waitStrategy = new HttpWaitStrategy().forPort(8080).forPath("/__admin").forStatusCode(200).withStartupTimeout(Duration.ofSeconds(10));

        addExposedPorts(WIREMOCK_HTTP_PORT, WIREMOCK_HTTPS_PORT);
    }

    /**
     * Returns the WireMockContainer object with WireMock stub configuration loaded.
     *
     * @param resource one or more resource paths that directo to either
     *                 a directory or a specific file
     * @return configured WireMockContainer
     */
    public WireMockContainer withStubMappingForClasspathResource(final String... resource) {
        this.stubsFromClasspath.addAll(Arrays.asList(resource));
        return this;
    }

    /**
     * Returns the WireMockContainer object with WireMock stub
     * configuration loaded.
     *
     * @param stub one or more valid WireMock json configurations
     * @return configured WireMockContainer
     */
    public WireMockContainer withStubMappingForString(final String... stub) {
        this.stubsFromTextBlock.addAll(Arrays.asList(stub));
        return this;
    }

    public int getHttpPort() {
        return getMappedPort(WIREMOCK_HTTP_PORT);
    }

    public int getHttpsPort() {
        return getMappedPort(WIREMOCK_HTTPS_PORT);
    }

    public String getHttpUrl() {
        return "http://" + this.getHost() + ":" + this.getHttpPort();
    }

    public String getHttpsUrl() {
        return "https://" + this.getHost() + ":" + this.getHttpsPort();
    }

    @Override
    protected void configure() {
        stubsFromClasspath.forEach(stub -> withClasspathResourceMapping(stub, MAPPING_DIR + stub, BindMode.READ_ONLY));
    }

    @Override
    @SneakyThrows({IOException.class, InterruptedException.class})
    public void start() {
        super.start();
        HttpClient httpClient = HttpClient.newHttpClient();
        for (String stubContent : stubsFromTextBlock) {
            HttpRequest httpRequest =
                    HttpRequest.newBuilder(URI.create(getHttpUrl() + "/__admin/mappings/new"))
                            .POST(HttpRequest.BodyPublishers.ofString(stubContent))
                            .build();
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        }
    }
}
