package com.naveen.api.base;

import com.naveen.api.config.ApiConfig;
import com.naveen.api.utils.TokenManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;

/**
 * BaseTest — parent class for all P2 test classes.
 *
 * Builds three shared RequestSpecifications once per suite:
 *   publicSpec  — for endpoints that need no auth
 *   authSpec    — for endpoints that need the token cookie
 *   baseSpec    — minimal spec for negative/security tests needing explicit control
 */
public class BaseTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    protected static final ApiConfig config = ConfigFactory.create(ApiConfig.class, System.getProperties());

    protected static RequestSpecification authSpec;
    protected static RequestSpecification publicSpec;
    protected static RequestSpecification baseSpec;
    protected static ResponseSpecification responseSpec;

    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        log.info("═══════════════════════════════════════");
        log.info("  P2 — API + Security Suite Starting");
        log.info("  Target: {}", config.baseUrl());
        log.info("═══════════════════════════════════════");

        RestAssured.baseURI = config.baseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Warm up token once for entire suite
        String token = TokenManager.getToken();
        log.info("Auth token ready: {}...{}",
                token.substring(0, 3),
                token.substring(token.length() - 3));

        buildSpecifications(token);

        log.info("BaseTest setup complete — all specs ready");
    }

    /**
     * SLA assertion helper — used by every test.
     * Fails the test if response time exceeds threshold in config.
     */
    protected void assertSla(long startMs, String endpoint) {
        long elapsed = System.currentTimeMillis() - startMs;
        if (elapsed > config.slaResponseTimeMs()) {
            throw new AssertionError(String.format(
                    "[SLA BREACH] %s responded in %dms — threshold is %dms",
                    endpoint, elapsed, config.slaResponseTimeMs()));
        }
        log.debug("[SLA PASS] {} — {}ms (threshold: {}ms)",
                endpoint, elapsed, config.slaResponseTimeMs());
    }

    private void buildSpecifications(String token) {

        // Base spec — shared foundation for all specs
        baseSpec = new RequestSpecBuilder()
                .setBaseUri(config.baseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        // Public spec — no auth, for GET /booking and POST /auth
        publicSpec = new RequestSpecBuilder()
                .addRequestSpecification(baseSpec)
                .build();

        // Auth spec — injects token cookie into every authenticated request
        authSpec = new RequestSpecBuilder()
                .addRequestSpecification(baseSpec)
                .addCookie("token", token)
                .build();

        // Response spec — common baseline assertions
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }
}