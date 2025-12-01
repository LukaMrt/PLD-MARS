package com.lukamaret.pld_mars_ihm.model;

import com.lukamaret.pld_mars_sma.model.Account;
import com.lukamaret.pld_mars_sma.service.AccountService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public class ListAccountsAction extends Action {
    private final AccountService service;

    public ListAccountsAction(AccountService service) {
        this.service = service;
    }

    @Override
    public void execute(HttpServletRequest request) {
        List<Account> accounts;

        try {
            accounts = service.getAllAccounts();
        } catch (Exception e) {
            request.setAttribute("code", 500);
            request.setAttribute("error", e.getMessage());
            return;
        }
        request.setAttribute("code", 200);
        request.setAttribute("accounts", accounts);
    }
}
