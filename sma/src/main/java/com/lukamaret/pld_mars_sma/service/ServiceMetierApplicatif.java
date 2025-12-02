package com.lukamaret.pld_mars_sma.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lukamaret.pld_mars_common.JsonHttpClient;
import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import com.lukamaret.pld_mars_sma.domain.Account;
import com.lukamaret.pld_mars_sma.domain.Address;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ServiceMetierApplicatif {
    protected final String accountSomUrl;
    protected final String addressSomUrl;
    protected final JsonHttpClient httpClient;
    protected final Gson gson;

    public ServiceMetierApplicatif(
            String accountSomUrl,
            String addressSomUrl,
            JsonHttpClient httpClient,
            Gson gson
    ) {
        this.accountSomUrl = accountSomUrl;
        this.addressSomUrl = addressSomUrl;
        this.httpClient = httpClient;
        this.gson = gson;
    }

    public List<Account> getAccountList() throws ServiceException {
        try {
            // 1. Get all accounts from OM-Account
            JsonObject accountsContainer = this.httpClient.post(
                    this.accountSomUrl,
                    new JsonHttpClient.Parameter("SOM", "listAccounts")
            );

            Type listType = new TypeToken<List<Account>>(){}.getType();
            List<Account> basicAccounts = gson.fromJson(accountsContainer.get("accounts"), listType);

            // 2. For each account, fetch addresses and construct enriched Account
            List<Account> enrichedAccounts = new ArrayList<>();
            for (Account basicAccount : basicAccounts) {
                List<Address> addresses = getAddressesForAccount(basicAccount.id);
                enrichedAccounts.add(new Account(basicAccount.id, basicAccount.name, addresses));
            }

            return enrichedAccounts;
        } catch (IOException e) {
            throw JsonServletHelper.ServiceObjectMetierCallException(this.accountSomUrl, "account", "listAccounts", e);
        }
    }

    private List<Address> getAddressesForAccount(int accountId) throws ServiceException {
        try {
            JsonObject addressesContainer = this.httpClient.post(
                    this.addressSomUrl,
                    new JsonHttpClient.Parameter("SOM", "listAddressesByAccount"),
                    new JsonHttpClient.Parameter("accountId", String.valueOf(accountId))
            );

            Type listType = new TypeToken<List<Address>>(){}.getType();
            return gson.fromJson(addressesContainer.get("addresses"), listType);
        } catch (IOException e) {
            throw JsonServletHelper.ServiceObjectMetierCallException(this.addressSomUrl, "address", "listAddressesByAccount", e);
        }
    }

    public void createAccount(String name) throws ServiceException {
        try {
            this.httpClient.post(
                    this.accountSomUrl,
                    new JsonHttpClient.Parameter("SOM", "addAccount"),
                    new JsonHttpClient.Parameter("name", name)
            );
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw JsonServletHelper.ServiceObjectMetierCallException(this.accountSomUrl, "account", "addAccount", e);
        }
    }

    public void createAddress(String street, String city, Integer accountId) throws ServiceException {
        try {
            this.httpClient.post(
                    this.addressSomUrl,
                    new JsonHttpClient.Parameter("SOM", "addAddress"),
                    new JsonHttpClient.Parameter("street", street),
                    new JsonHttpClient.Parameter("city", city),
                    new JsonHttpClient.Parameter("accountId", String.valueOf(accountId))
            );
        } catch (IOException e) {
            throw JsonServletHelper.ServiceObjectMetierCallException(this.addressSomUrl, "address", "addAddress", e);
        }
    }
}
