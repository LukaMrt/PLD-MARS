package com.lukamaret.pld_mars_sma.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lukamaret.pld_mars_common.JsonHttpClient;
import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import com.lukamaret.pld_mars_sma.domain.Account;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ServiceMetierApplicatif {
    protected final String accountSomUrl;
    protected final JsonHttpClient httpClient;
    protected final Gson gson;

    public ServiceMetierApplicatif(
            String accountSomUrl,
            JsonHttpClient httpClient,
            Gson gson
    ) {
        this.accountSomUrl = accountSomUrl;
        this.httpClient = httpClient;
        this.gson = gson;
    }

    public List<Account> getAccountList() throws ServiceException {
        try {
            JsonObject accountsContainer = this.httpClient.post(
                    this.accountSomUrl,
                    new JsonHttpClient.Parameter("SOM", "listAccounts")
            );

            Type listType = new TypeToken<List<Account>>(){}.getType();

            return gson.fromJson(accountsContainer.get("accounts"), listType);
        } catch (IOException e) {
            throw JsonServletHelper.ServiceObjectMetierCallException(this.accountSomUrl, "account", "listAccounts", e);
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
}
