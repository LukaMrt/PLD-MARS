package com.lukamaret.pld_mars_address.console;

import com.lukamaret.pld_mars_address.domain.Address;
import com.lukamaret.pld_mars_address.service.AddressService;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        AddressService addressService = new AddressService();

        try {
            // Test: Create an address
            addressService.createAddress("123 Main St", "Paris", 1);
            System.out.println("Address created successfully!");

            // Test: Get addresses by account
            List<Address> addresses = addressService.getAddressesByAccountId(1);
            System.out.println("Found " + addresses.size() + " address(es) for account 1");

            for (Address address : addresses) {
                System.out.println("- " + address.getStreet() + ", " + address.getCity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
