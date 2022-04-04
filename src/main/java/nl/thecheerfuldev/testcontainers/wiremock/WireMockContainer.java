package nl.thecheerfuldev.testcontainers.wiremock;

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

public class WireMockContainer<SELF extends WireMockContainer<SELF>> extends GenericContainer<SELF> {

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

    public SELF withStubMappingForClasspathResource(final String... resource) {
        this.stubsFromClasspath.addAll(Arrays.asList(resource));
        return self();
    }

    public SELF withStubMappingForString(final String... stub) {
        this.stubsFromTextBlock.addAll(Arrays.asList(stub));
        return self();
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
    public void start() {
        super.start();
        HttpClient httpClient = HttpClient.newHttpClient();
        stubsFromTextBlock.forEach(stubContent -> {
            HttpRequest httpRequest =
                    HttpRequest.newBuilder(URI.create(getHttpUrl() + "/__admin/mappings/new"))
                            .POST(HttpRequest.BodyPublishers.ofString(stubContent))
                            .build();
            try {
                httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
