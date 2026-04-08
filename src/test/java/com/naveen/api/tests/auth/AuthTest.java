package com.naveen.api.tests.auth;

import com.naveen.api.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Auth Tests — POST /auth
 * Smoke test that confirms the entire foundation works:
 * BaseTest, TokenManager, RequestSpec, SLA assertion.
 */
@Epic("P2 — API + Security Suite")
@Feature("Authentication")
public class AuthTest extends BaseTest {

    private static final String AUTH_ENDPOINT = "/auth";

    @Test(groups = {"smoke", "auth"})
    @Story("Valid credentials return a token")
    @Description("POST /auth with correct credentials must return 200 and a non-empty token within SLA")
    @Severity(SeverityLevel.BLOCKER)
    public void validCredentials_shouldReturnToken() {
        long start = System.currentTimeMillis();

        Response response = given()
                .spec(publicSpec)
                .body(Map.of(
                        "username", config.adminUsername(),
                        "password", config.adminPassword()
                ))
                .when()
                .post(AUTH_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertSla(start, "POST /auth");

        String token = response.path("token");

        assertThat(token)
                .as("Token must not be null or empty")
                .isNotNull()
                .isNotEmpty();

        log.info("Smoke test passed — token received: {}...{}",
                token.substring(0, 3),
                token.substring(token.length() - 3));
    }

    @Test(groups = {"negative", "auth"})
    @Story("Invalid credentials do not return a token")
    @Description("POST /auth with wrong password must return Bad credentials — not a valid token")
    @Severity(SeverityLevel.CRITICAL)
    public void invalidCredentials_shouldNotReturnToken() {
        Response response = given()
                .spec(publicSpec)
                .body(Map.of(
                        "username", config.adminUsername(),
                        "password", "WRONG_PASSWORD"
                ))
                .when()
                .post(AUTH_ENDPOINT)
                .then()
                .statusCode(200)
                .extract()
                .response();

        String reason = response.path("reason");
        String token  = response.path("token");

        assertThat(reason)
                .as("Response must contain Bad credentials reason")
                .isEqualTo("Bad credentials");

        assertThat(token)
                .as("Token must not be returned for invalid credentials")
                .isNull();

        log.info("Invalid credentials correctly rejected — reason: {}", reason);
    }
}