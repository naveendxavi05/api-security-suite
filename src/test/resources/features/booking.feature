Feature: Booking API — BDD scenarios

  Background:
    Given the booking API is available

  Scenario: Get all bookings returns a non-empty list
    When I send a GET request to "/booking"
    Then the response status code should be 200
    And the response should contain a list of booking IDs

  Scenario: Get a specific booking by ID
    When I send a GET request to "/booking/1"
    Then the response status code should be 200
    And the response should contain field "firstname"
    And the response should contain field "lastname"

  Scenario: Create a new booking
    When I create a new booking with valid details
    Then the response status code should be 200
    And the response should contain a booking ID

  Scenario: Delete booking without auth should fail
    When I send a DELETE request to "/booking/1" without auth
    Then the response status code should be 403

  Scenario: Auth with wrong password should return Bad credentials
    When I authenticate with username "admin" and password "wrongpass"
    Then the response status code should be 200
    And the response body should contain "Bad credentials"