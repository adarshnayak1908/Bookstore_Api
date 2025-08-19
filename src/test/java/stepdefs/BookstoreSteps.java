package stepdefs;

import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import pojo.LoginRequest;
import pojo.LoginResponse;
import utils.AuthRetryFilter;
import utils.BookDataGenerator;
import utils.ConfigReader;
import utils.JsonFileReader;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BookstoreSteps {
    private static final ConfigReader CONFIG = new ConfigReader("src/test/resources/config.properties");
    private static final String BASE_URL = (CONFIG.get("base_uri") != null ? CONFIG.get("base_uri").trim() : "http://localhost:8000");

    // Install baseURI + token auto-retry filter globally; no changes to step methods required.
    static {
        RestAssured.baseURI = System.getProperty("api.base", BASE_URL);
        RestAssured.filters(new AuthRetryFilter());
    }

    private Response response;
    private String jwtToken;

    /**
     * Logs a message to Extent report if available,
     * otherwise logs to console (prevents NullPointerException when Extent isn't initialized).
     */
    private void logSafe(String message) {
        try {
            ExtentCucumberAdapter.addTestStepLog(message);
        }catch (Throwable t) {
            // Extent not ready, ignore
        }
        System.out.println(message);
    }


    /**
     * Sends HTTP request with optional JWT token and optional request body.
     * Logs request and response in Extent Report.
     *
     * @param method   HTTP method (GET, POST, PUT, DELETE)
     * @param endpoint API endpoint
     * @param body     Request payload (nullable)
     * @return Response from the API
     */
    private Response sendRequest(String method, String endpoint, String body) {
        try {
            RequestSpecification request = given()
                    .header("Content-Type", "application/json");

            if (jwtToken != null) {
                request.header("Authorization", "Bearer " + jwtToken);
            }

            if (body != null && !body.trim().isEmpty()) {
                request.body(body);
            }

            logSafe("📤 Request: " + method + " " + endpoint);
            if (body != null && !body.trim().isEmpty()) {
                logSafe("📦 Payload:\n" + body);
            }

            Response resp = switch (method.toUpperCase()) {
                case "GET" -> request.get(endpoint);
                case "POST" -> request.post(endpoint);
                case "PUT" -> request.put(endpoint);
                case "DELETE" -> request.delete(endpoint);
                default -> throw new IllegalArgumentException("Unsupported method: " + method);
            };

            logSafe("📥 Response Status: " + resp.getStatusCode());
            logSafe("📄 Response Body:\n" + resp.getBody().asPrettyString());

            return resp;

        } catch (Exception e) {
            logSafe("❌ Request failed: " + e.getMessage());
            throw e;
        }
    }

    // -------------------
    // Login Steps
    // -------------------

    /** Login using credentials from config file. */
    @When("I login using valid credentials")
    public void loginWithConfig() throws JsonProcessingException {
        LoginRequest loginRequest = new LoginRequest(
                Integer.parseInt(CONFIG.get("id")),
                CONFIG.get("email"),
                CONFIG.get("password")
        );

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(loginRequest);

        response = given()
                .header("Content-Type", "application/json")
                .body(body)
                .post(BASE_URL + "/login");

        assertEquals(200, response.getStatusCode(), "Login should be successful");

        LoginResponse loginResponse = mapper.readValue(response.getBody().asString(), LoginResponse.class);
        System.out.println("JWT Token: " + loginResponse.getAccess_token());
    }

    /** Login using a JSON file containing credentials. */
    @When("I send a POST request to signup with body {string}")
    public void signUp(String fileName) throws Exception {
        String body = JsonFileReader.readJsonFromFile(fileName);
        response = sendRequest("POST", BASE_URL + "/login", body);
    }

    @When("I send a POST request to login with body {string}")
    public void loginWithJsonFile(String fileName) throws Exception {
        String body = JsonFileReader.readJsonFromFile(fileName);
        response = sendRequest("POST", BASE_URL + "/login", body);
    }


    /**
     * Creates a book using either a random payload or a payload from a JSON file.
     *
     * @param payloadSource "RANDOM" for generated data or filename for JSON payload.
     */
    @When("I send a POST request to books with {string} and JWT")
    public void createBook(String payloadSource) throws Exception {
        String body;

        if (payloadSource.equalsIgnoreCase("RANDOM")) {
            body = new ObjectMapper().writeValueAsString(BookDataGenerator.generateRandomBook());
        } else {
            body = JsonFileReader.readJsonFromFile(payloadSource);
        }

        response = sendRequest("POST", BASE_URL + "/books/", body);
    }

    /**
     * Retrieves all books using JWT authentication.
     */
    @When("I send a GET request to books")
    public void getBooks() {
        response = sendRequest("GET", BASE_URL + "/books/", null);
    }

    /**
     * Retrieves a single book by ID.
     *
     * @param bookId Book ID to fetch.
     */
    @When("I send a GET request to books with id {int}")
    public void getBookById(int bookId) {
        response = sendRequest("GET", BASE_URL + "/books/" + bookId, null);
    }

    /**
     * Updates a book by ID.
     *
     * @param bookId        Book ID to update.
     * @param payloadSource "RANDOM" for generated data or filename for JSON payload.
     */
    @When("I send a PUT request to books with id {int} using {string}")
    public void updateBook(int bookId, String payloadSource) throws Exception {
        String body;

        if (payloadSource.equalsIgnoreCase("RANDOM")) {
            body = new ObjectMapper().writeValueAsString(BookDataGenerator.generateRandomBook());
        } else {
            body = JsonFileReader.readJsonFromFile(payloadSource);
        }

        response = sendRequest("PUT", BASE_URL + "/books/" + bookId, body);
    }

    /**
     * Deletes a book by ID.
     *
     * @param bookId Book ID to delete.
     */
    @When("I send a DELETE request to books with id {int}")
    public void deleteBook(int bookId) {
        response = sendRequest("DELETE", BASE_URL + "/books/" + bookId, null);
    }

    /**
     * Verifies the API response status code.
     *
     * @param expectedStatus Expected HTTP status code.
     */
    @Then("the response status should be {int}")
    public void verifyStatus(int expectedStatus) {
        assertEquals(expectedStatus, response.getStatusCode(),
                "Expected status " + expectedStatus + " but got " + response.getStatusCode());
    }

    @Then("the response should contain at least {int} book")
    public void verifyBookCount(int minCount) {
        List<Map<String, Object>> books = response.jsonPath().getList("$");
        assertTrue(books.size() >= minCount, "Expected at least " + minCount + " books");
    }

    @Then("the first book should have a name and author")
    public void verifyFirstBookFields() {
        Map<String, Object> firstBook = response.jsonPath().getMap("[0]");
        assertNotNull(firstBook.get("name"), "Book name should not be null");
        assertNotNull(firstBook.get("author"), "Book author should not be null");
    }
}
