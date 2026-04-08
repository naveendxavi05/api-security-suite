package com.naveen.api.utils;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Factory that generates dynamic, unique booking payloads for every test run.
 * Zero hardcoded strings — every call returns fresh data via JavaFaker.
 * Safe for parallel execution — no shared state between calls.
 */
public class BookingPayloadFactory {

    private static final Logger log = LoggerFactory.getLogger(BookingPayloadFactory.class);
    private static final Faker faker = new Faker();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private BookingPayloadFactory() {}

    /**
     * Generates a complete valid booking payload.
     * Every call returns a unique booking — no shared state.
     */
    public static Map<String, Object> validBooking() {
        LocalDate checkin  = LocalDate.now().plusDays(new Random().nextInt(30) + 1);
        LocalDate checkout = checkin.plusDays(new Random().nextInt(7) + 1);

        Map<String, Object> bookingDates = new HashMap<>();
        bookingDates.put("checkin",  checkin.format(DATE_FMT));
        bookingDates.put("checkout", checkout.format(DATE_FMT));

        Map<String, Object> booking = new HashMap<>();
        booking.put("firstname",       faker.name().firstName());
        booking.put("lastname",        faker.name().lastName());
        booking.put("totalprice",      faker.number().numberBetween(50, 500));
        booking.put("depositpaid",     faker.bool().bool());
        booking.put("bookingdates",    bookingDates);
        booking.put("additionalneeds", faker.lorem().sentence());

        log.debug("Generated valid booking: {} {}",
                booking.get("firstname"), booking.get("lastname"));
        return booking;
    }

    /**
     * Partial update payload — only fields PATCH should modify.
     */
    public static Map<String, Object> partialUpdate() {
        Map<String, Object> patch = new HashMap<>();
        patch.put("firstname",   faker.name().firstName());
        patch.put("totalprice",  faker.number().numberBetween(100, 999));
        return patch;
    }

    /**
     * Payload with missing required fields — used in negative tests.
     * firstname and lastname intentionally omitted — expects 400.
     */
    public static Map<String, Object> missingRequiredFields() {
        Map<String, Object> booking = new HashMap<>();
        booking.put("totalprice",  faker.number().numberBetween(50, 500));
        booking.put("depositpaid", true);
        return booking;
    }

    /**
     * SQL injection payload — security test.
     * Expects API to return 400 or sanitise — never 500.
     */
    public static Map<String, Object> sqlInjectionPayload() {
        Map<String, Object> booking = validBooking();
        booking.put("firstname",       "' OR '1'='1");
        booking.put("lastname",        "'; DROP TABLE bookings; --");
        booking.put("additionalneeds", "1=1; SELECT * FROM users");
        log.warn("SQL injection payload generated — security test only");
        return booking;
    }

    /**
     * XSS payload — security test.
     * Expects API to sanitise or reject — never reflect unescaped script.
     */
    public static Map<String, Object> xssPayload() {
        Map<String, Object> booking = validBooking();
        booking.put("firstname",       "<script>alert('XSS')</script>");
        booking.put("lastname",        "<img src=x onerror=alert(1)>");
        booking.put("additionalneeds", "javascript:alert(document.cookie)");
        log.warn("XSS payload generated — security test only");
        return booking;
    }

    /**
     * Malformed JSON string — used to test API handling of bad input.
     * Send as raw string, bypassing serialisation intentionally.
     */
    public static String malformedJson() {
        return "{\"firstname\": \"Test\", \"lastname\": }";
    }
}