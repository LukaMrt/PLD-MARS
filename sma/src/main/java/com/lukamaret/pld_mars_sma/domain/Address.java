package com.lukamaret.pld_mars_sma.domain;

public class Address {
    public final int id;
    public final String street;
    public final String city;
    public final int accountId;

    public Address(int id, String street, String city, int accountId) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.accountId = accountId;
    }
}
