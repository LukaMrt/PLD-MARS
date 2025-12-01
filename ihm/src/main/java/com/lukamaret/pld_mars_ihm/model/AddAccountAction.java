package com.lukamaret.pld_mars_ihm.model;

import com.google.gson.JsonObject;
import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import com.lukamaret.pld_mars_ihm.service.IhmService;
import jakarta.servlet.http.HttpServletRequest;

public class AddAccountAction extends Action {
    private final IhmService service;

    public AddAccountAction(IhmService service) {
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