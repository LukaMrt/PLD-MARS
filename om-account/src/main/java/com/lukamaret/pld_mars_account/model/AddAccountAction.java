package com.lukamaret.pld_mars_account.model;

import com.google.gson.JsonObject;
import com.lukamaret.pld_mars_account.service.AccountService;
import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;

public class AddAccountAction extends Action {
    private final AccountService service;

    public AddAccountAction(AccountService service) {
        this.service = service;
    }

    @Override
    public void execute(HttpServletRequest request) throws ServiceException {
        String name;
        JsonObject body = getBody(request);
        if (body == null) {
            name = request.getParameter("name");
        } else {
            name = body.get("name").getAsString();
        }

        try {
            service.createAccount(name);
        } catch (Exception e) {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("account", "addAccount", e);
        }

        request.setAttribute("code", 200);
    }
}