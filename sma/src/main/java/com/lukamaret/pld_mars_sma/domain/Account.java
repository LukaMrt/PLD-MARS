package com.lukamaret.pld_mars_sma.domain;

import java.util.List;

public class Account {
    public final int id;
    public final String name;
    public final List<Address> addresses;

    public Account(int id, String name, List<Address> addresses) {
        this.id = id;
        this.name = name;
        this.addresses = addresses;
    }
}