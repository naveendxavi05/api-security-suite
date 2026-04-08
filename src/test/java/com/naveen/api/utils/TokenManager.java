package com.naveen.api.utils;

import com.naveen.api.config.ApiConfig;
import io.restassured.http.ContentType;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static io.restassured.RestAssured.given;

/**
 * Thread-safe token manager using double-checked locking.
 * Token is fetched ONCE per suite and reused by all authenticated tests.
 * Not fetched fresh per test — avoids 50+ unnecessary auth calls in CI.
 */
public class TokenManager {

    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);
    private static final ApiConfig config = ConfigFactory.create(ApiConfig.class, System.getProperties());
    private static final ReentrantLock lock = new ReentrantLock();

    private static volatile String token = null;

    private TokenManager() {}

    /**
     * Returns valid auth token. Fetches from API only if not already cached.
     * Thread-safe via double-checked locking.
     */
    public static String getToken() {
        if (token == null) {
            lock.lock();
            try {
                if (token == null) {
                    log.info("No token cached — fetching from POST /auth");
                    token = fetchNewToken();
                    log.info("Token acquired: {}...{}",
                            token.substring(0, 3),
                            token.substring(token.length() - 3));
                }
            } finally {
                lock.unlock();
            }
        }
        return token;
    }

    /**
     * Force-refreshes the token.
     * Call this when a request returns 403 due to token expiry.
     */
    public static String refreshToken() {
        lock.lock();
        try {
            log.info("Force-refreshing token via POST /auth");
            token = fetchNewToken();
            return token;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Clears the cached token.
     * Used in negative auth tests to force a re-fetch.
     */
    public static void clearToken() {
        lock.lock();
        try {
            token = null;
            log.info("Token cache cleared");
        } finally {
            lock.unlock();
        }
    }

    private static String fetchNewToken() {
        String fetchedToken = given()
                .baseUri(config.baseUrl())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", config.adminUsername(),
                        "password", config.adminPassword()
                ))
                .when()
                .post("/auth")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        if (fetchedToken == null || fetchedToken.isBlank()) {
            throw new RuntimeException(
                    "Token fetch returned null or empty — check credentials in config.properties");
        }
        return fetchedToken;
    }
}