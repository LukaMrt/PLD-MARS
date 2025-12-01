package com.lukamaret.pld_mars_account.model;

import com.lukamaret.pld_mars_account.domain.Account;
import com.lukamaret.pld_mars_account.service.AccountService;
import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public class ListAccountsAction extends Action {
    private final AccountService service;

    public ListAccountsAction(AccountService service) {
        this.service = service;
    }

    @Override
    public void execute(HttpServletRequest request) throws ServiceException {
        List<Account> accounts;
        try {
            accounts = service.getAllAccounts();
        } catch (Exception e) {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("account", "listAccounts", e);
        }
        request.setAttribute("code", 200);
        request.setAttribute("accounts", accounts);
    }
}
