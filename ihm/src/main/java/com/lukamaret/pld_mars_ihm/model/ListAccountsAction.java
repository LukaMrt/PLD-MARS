package com.lukamaret.pld_mars_ihm.model;


import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import com.lukamaret.pld_mars_ihm.domain.Account;
import com.lukamaret.pld_mars_ihm.service.IhmService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public class ListAccountsAction extends Action {
    private final IhmService service;

    public ListAccountsAction(IhmService service) {
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
