package com.naveen.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Booking response model — deserialised from API responses using Jackson.
 * @JsonIgnoreProperties(ignoreUnknown = true) means new fields added
 * to the API won't break existing tests — resilient by design.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Booking {

    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("lastname")
    private String lastname;

    @JsonProperty("totalprice")
    private Integer totalprice;

    @JsonProperty("depositpaid")
    private Boolean depositpaid;

    @JsonProperty("bookingdates")
    private BookingDates bookingdates;

    @JsonProperty("additionalneeds")
    private String additionalneeds;

    // Getters
    public String getFirstname()          { return firstname; }
    public String getLastname()           { return lastname; }
    public Integer getTotalprice()        { return totalprice; }
    public Boolean getDepositpaid()       { return depositpaid; }
    public BookingDates getBookingdates() { return bookingdates; }
    public String getAdditionalneeds()    { return additionalneeds; }

    // Setters
    public void setFirstname(String firstname)                 { this.firstname = firstname; }
    public void setLastname(String lastname)                   { this.lastname = lastname; }
    public void setTotalprice(Integer totalprice)              { this.totalprice = totalprice; }
    public void setDepositpaid(Boolean depositpaid)            { this.depositpaid = depositpaid; }
    public void setBookingdates(BookingDates bookingdates)     { this.bookingdates = bookingdates; }
    public void setAdditionalneeds(String additionalneeds)     { this.additionalneeds = additionalneeds; }

    @Override
    public String toString() {
        return "Booking{firstname='" + firstname + "', lastname='" + lastname +
                "', totalprice=" + totalprice + ", depositpaid=" + depositpaid + "}";
    }

    // Inner model
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookingDates {

        @JsonProperty("checkin")
        private String checkin;

        @JsonProperty("checkout")
        private String checkout;

        public String getCheckin()  { return checkin; }
        public String getCheckout() { return checkout; }
        public void setCheckin(String checkin)   { this.checkin = checkin; }
        public void setCheckout(String checkout) { this.checkout = checkout; }
    }
}