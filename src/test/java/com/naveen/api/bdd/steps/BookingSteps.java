package com.naveen.api.bdd.steps;

import com.naveen.api.config.ApiConfig;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import org.aeonbits.owner.ConfigFactory;
import org.assertj.core.api.Assertions;

import static io.restassured.RestAssured.given;

public class BookingSteps {

    private Response response;
    private final ApiConfig config = ConfigFactory.create(ApiConfig.class, System.getProperties());

    @Before
    public void setup() {
        io.restassured.RestAssured.baseURI = config.baseUrl();
    }

    // ─────────────────────────────────────────────
    // Background
    // ─────────────────────────────────────────────

    @Given("the booking API is available")
    public void theBookingApiIsAvailable() {
        Response health = given()
                .when()
                .get("/booking");
        Assertions.assertThat(health.statusCode())
                .as("API should be reachable")
                .isEqualTo(200);
    }

    // ─────────────────────────────────────────────
    // When steps
    // ─────────────────────────────────────────────

    @When("I send a GET request to {string}")
    public void iSendAGetRequestTo(String path) {
        response = given()
                .header("Accept", "application/json")
                .when()
                .get(path);
    }

    @When("I create a new booking with valid details")
    public void iCreateANewBookingWithValidDetails() {
        String body = """
                {
                    "firstname": "Naveen",
                    "lastname": "QA",
                    "totalprice": 200,
                    "depositpaid": true,
                    "bookingdates": {
                        "checkin": "2026-05-01",
                        "checkout": "2026-05-05"
                    },
                    "additionalneeds": "BDD Test"
                }
                """;

        response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(body)
                .when()
                .post("/booking");
    }

    @When("I send a DELETE request to {string} without auth")
    public void iSendADeleteRequestWithoutAuth(String path) {
        response = given()
                .header("Accept", "application/json")
                .when()
                .delete(path);
    }

    @When("I authenticate with username {string} and password {string}")
    public void iAuthenticateWithUsernameAndPassword(String username, String password) {
        String body = String.format("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """, username, password);

        response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(body)
                .when()
                .post("/auth");
    }

    // ─────────────────────────────────────────────
    // Then steps
    // ─────────────────────────────────────────────

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatus) {
        Assertions.assertThat(response.statusCode())
                .as("Expected status %d but got %d", expectedStatus, response.statusCode())
                .isEqualTo(expectedStatus);
    }

    @Then("the response should contain a list of booking IDs")
    public void theResponseShouldContainAListOfBookingIDs() {
        Assertions.assertThat(response.jsonPath().getList("bookingid"))
                .as("Response should contain booking IDs")
                .isNotEmpty();
    }

    @Then("the response should contain field {string}")
    public void theResponseShouldContainField(String field) {
        Assertions.assertThat(response.jsonPath().getString(field))
                .as("Response should contain field: " + field)
                .isNotNull();
    }

    @Then("the response should contain a booking ID")
    public void theResponseShouldContainABookingID() {
        if (response.statusCode() == 418) {
            throw new org.testng.SkipException("Rate limited — skipping create booking BDD test");
        }
        Assertions.assertThat(response.jsonPath().getInt("bookingid"))
                .as("Response should contain a bookingid")
                .isGreaterThan(0);
    }

    @Then("the response body should contain {string}")
    public void theResponseBodyShouldContain(String text) {
        Assertions.assertThat(response.getBody().asString())
                .as("Response body should contain: " + text)
                .contains(text);
    }
}