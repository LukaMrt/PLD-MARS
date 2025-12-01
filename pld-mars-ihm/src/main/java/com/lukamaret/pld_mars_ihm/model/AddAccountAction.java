package com.lukamaret.pld_mars_ihm.model;

import com.lukamaret.pld_mars_sma.service.AccountService;

import jakarta.servlet.http.HttpServletRequest;

public class AddAccountAction extends Action {
    private final AccountService service;

    public AddAccountAction(AccountService service) {
        this.service = service;
    }

    @Override
    public void execute(HttpServletRequest request) {
        String name = getBody(request).get("name").getAsString();

        try {
            service.createAccount(name);
            request.setAttribute("code", 201);
        } catch (Exception e) {
            request.setAttribute("code", 500);
            request.setAttribute("error", e.getMessage());
        }
    }
}