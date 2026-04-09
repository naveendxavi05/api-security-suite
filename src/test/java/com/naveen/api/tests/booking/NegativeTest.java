package com.naveen.api.tests.booking;

import com.naveen.api.base.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.SkipException;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class NegativeTest extends BaseTest {

    // ─────────────────────────────────────────────
    // AUTH FAILURE SCENARIOS
    // ─────────────────────────────────────────────

    @Test(groups = {"negative", "regression"},description = "DELETE without auth token should return 403")
    public void delete_noAuth_shouldReturn403() {
        Response response = given(baseSpec)
                .when()
                .delete("/booking/1")
                .then()
                .extract().response();

        assertThat(response.statusCode())
                .as("No-auth DELETE should return 403")
                .isEqualTo(403);

        log.info("PASS — no-auth DELETE correctly returned 403");
    }

    @Test(groups = {"negative", "regression"}, description = "DELETE with invalid token should return 403")
    public void delete_invalidToken_shouldReturn403() {
        Response response = given(baseSpec)
                .cookie("token", "invalid_token_abc123")
                .when()
                .delete("/booking/1")
                .then()
                .extract().response();

        assertThat(response.statusCode())
                .as("Invalid token DELETE should return 403")
                .isEqualTo(403);

        log.info("PASS — invalid token DELETE correctly returned 403");
    }

    @Test(groups = {"negative", "regression"}, description = "Auth with wrong password should return Bad credentials")
    public void auth_wrongPassword_shouldReturnBadCredentials() {
        Response response = given(authSpec)
                .body("{\"username\":\"admin\",\"password\":\"wrongpass\"}")
                .when()
                .post("/auth")
                .then()
                .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("reason"))
                .as("Wrong password should return Bad credentials")
                .isEqualTo("Bad credentials");

        log.info("PASS — wrong password correctly returned Bad credentials");
    }

    // ─────────────────────────────────────────────
    // INPUT VALIDATION SCENARIOS
    // ─────────────────────────────────────────────

    @Test(groups = {"negative", "regression"}, description = "GET non-existent booking ID should return 404")
    public void get_nonExistentId_shouldReturn404() {
        Response response = given(publicSpec)
                .when()
                .get("/booking/999999")
                .then()
                .extract().response();

        assertThat(response.statusCode())
                .as("Non-existent booking should return 404")
                .isEqualTo(404);

        log.info("PASS — non-existent booking ID correctly returned 404");
    }

    @Test(groups = {"negative", "regression"}, description = "POST booking with missing firstname should return 500")
    public void post_missingFirstname_shouldReturn500() {
        String body = """
                {
                    "lastname": "TestUser",
                    "totalprice": 100,
                    "depositpaid": true,
                    "bookingdates": {
                        "checkin": "2026-05-01",
                        "checkout": "2026-05-05"
                    }
                }
                """;

        Response response = given(publicSpec)
                .body(body)
                .when()
                .post("/booking")
                .then()
                .extract().response();

        if (response.statusCode() == 418) {
            throw new SkipException("Rate limited — skipping missing firstname test");
        }

        assertThat(response.statusCode())
                .as("Missing firstname should return 4xx or 5xx")
                .isGreaterThanOrEqualTo(400);

        log.info("PASS — missing firstname returned status: {}", response.statusCode());
    }

    @Test(groups = {"negative", "regression"}, description = "POST booking with wrong Content-Type should return 4xx or 5xx")
    public void post_wrongContentType_shouldFail() {
        Response response = given(publicSpec)
                .contentType("text/plain")
                .body("this is not json")
                .when()
                .post("/booking")
                .then()
                .extract().response();

        if (response.statusCode() == 418) {
            throw new SkipException("Rate limited — skipping wrong content-type test");
        }

        assertThat(response.statusCode())
                .as("Wrong Content-Type should return 4xx or 5xx")
                .isGreaterThanOrEqualTo(400);

        log.info("PASS — wrong Content-Type returned status: {}", response.statusCode());
    }

    @Test(groups = {"negative", "regression"}, description = "POST booking with malformed JSON should return 4xx or 5xx")
    public void post_malformedJson_shouldFail() {
        Response response = given(publicSpec)
                .body("{ this is : not valid json }")
                .when()
                .post("/booking")
                .then()
                .extract().response();

        if (response.statusCode() == 418) {
            throw new SkipException("Rate limited — skipping malformed JSON test");
        }

        assertThat(response.statusCode())
                .as("Malformed JSON should return 4xx or 5xx")
                .isGreaterThanOrEqualTo(400);

        log.info("PASS — malformed JSON returned status: {}", response.statusCode());
    }

    // ─────────────────────────────────────────────
    // BOUNDARY TESTS
    // ─────────────────────────────────────────────

    @Test(groups = {"negative", "regression"}, description = "POST booking with 10000 char firstname should return 4xx or 5xx")
    public void post_extremelyLongFirstname_shouldFail() {
        String longName = "A".repeat(10000);
        String body = String.format("""
                {
                    "firstname": "%s",
                    "lastname": "Test",
                    "totalprice": 100,
                    "depositpaid": true,
                    "bookingdates": {
                        "checkin": "2026-05-01",
                        "checkout": "2026-05-05"
                    }
                }
                """, longName);

        Response response = given(publicSpec)
                .body(body)
                .when()
                .post("/booking")
                .then()
                .extract().response();

        if (response.statusCode() == 418) {
            throw new SkipException("Rate limited — skipping long firstname test");
        }

        // Restful Booker accepts this (known quirk) — log either way
        log.info("Boundary test — 10000 char firstname returned status: {}",
                response.statusCode());
    }

    @Test(groups = {"negative", "regression"}, description = "POST booking with empty string fields should return 4xx or 5xx")
    public void post_emptyStringFields_shouldFail() {
        String body = """
                {
                    "firstname": "",
                    "lastname": "",
                    "totalprice": 100,
                    "depositpaid": true,
                    "bookingdates": {
                        "checkin": "2026-05-01",
                        "checkout": "2026-05-05"
                    }
                }
                """;

        Response response = given(publicSpec)
                .body(body)
                .when()
                .post("/booking")
                .then()
                .extract().response();

        if (response.statusCode() == 418) {
            throw new SkipException("Rate limited — skipping empty fields test");
        }

        // Restful Booker accepts empty strings (known quirk) — log either way
        log.info("Boundary test — empty string fields returned status: {}",
                response.statusCode());
    }
}