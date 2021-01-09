import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.http.Header;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.*;

public class MockApiTests {

    WireMockServer wireMockServer;

    @Before
    public void configure(){
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        setupStub();
    }

    @After
    public void teardown(){
        wireMockServer.stop();
    }

    @Test
    public void getAllBooks() {
        given().
        when().
                get("http://localhost:8080/api/books").
        then().
                assertThat().
                statusCode(200);
    }

    @Test
    public void getAValidBook() {
        given().
        when().
                get("http://localhost:8080/api/books/1").
        then().
                assertThat().
                statusCode(200);
    }

    @Test
    public void notGetAInvalidBook() {
        given().
        when().
                get("http://localhost:8080/api/books/3").
        then().
                assertThat().
                statusCode(400);
    }

    @Test
    public void addANewBook() {
        given().
                header(new Header("Accept", "application/json")).
                body("{ \"author\": \"Howard Schultz\", \"title\": \"Starbucks\" }").
        when().
                put("http://localhost:8090/api/books/4").
        then().
                assertThat().
                statusCode(200);
    }

    @Test
    public void notAddNonAuthorBook() {
        given().
                header(new Header("Accept", "application/json")).
                body("{ \"title\": \"Starbucks\" }").
        when().
                put("http://localhost:8090/api/books/5").
        then().
                assertThat().
                statusCode(400);
    }

    public void setupStub(){
        configureFor("localhost", 8080);

        stubFor(get(urlEqualTo("/api/books"))
                .inScenario("getAllBooks")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{ \"id\": 1, \"author\": \"John Smith\", \"title\": \"SRE 101\" }, { \"author\": \"Jane Archer\", \"title\": \"DevOps is a lie\" }")));

        stubFor(get(urlEqualTo("/api/books/1"))
                .inScenario("getAllBooks")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{ \"id\": 1, \"author\": \"John Smith\", \"title\": \"SRE 101\" }")));

        stubFor(get(urlEqualTo("/api/books/3"))
                .inScenario("notGetAInvalidBook")
                .withRequestBody(equalToJson("{\"wrongparameter\":\"wrongvalue\"}"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "text/html")
                        .withBody("Error response body")));


        stubFor(put(urlEqualTo("/api/books/4"))
                .inScenario("addANewBook")
                .withHeader("Accept", matching("application/json"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"id\": 3, \"author\": \"Howard Schultz\", \"title\": \"Starbucks\" }")
                        .withStatus(200)));

        stubFor(put(urlEqualTo("/api/books/5"))
                .inScenario("notAddNonAuthorBook")
                .withHeader("Accept", matching("application/json"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withBody("error : Field 'author' is required")
                        .withStatus(400)));

    }

}
