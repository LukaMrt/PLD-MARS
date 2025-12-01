package com.lukamaret.pld_mars_sma.model;

import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import com.lukamaret.pld_mars_sma.domain.Account;
import com.lukamaret.pld_mars_sma.service.ServiceMetierApplicatif;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public class ListAccountsAction extends Action {
    private final ServiceMetierApplicatif service;

    public ListAccountsAction(ServiceMetierApplicatif service) {
        this.service = service;
    }

    @Override
    public void execute(HttpServletRequest request) throws ServiceException {
        List<Account> accounts;
        try {
            accounts = service.getAccountList();
        } catch (Exception e) {
            throw JsonServletHelper.ServiceMetierExecutionException("listAccounts", e);
        }
        request.setAttribute("code", 200);
        request.setAttribute("accounts", accounts);
    }
}
