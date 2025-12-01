package com.lukamaret.pld_mars_account.controller;

import com.lukamaret.pld_mars_account.model.AddAccountAction;
import com.lukamaret.pld_mars_account.model.ListAccountsAction;
import com.lukamaret.pld_mars_account.service.AccountService;
import com.lukamaret.pld_mars_account.utils.JpaUtil;
import com.lukamaret.pld_mars_account.vue.AddAccountVue;
import com.lukamaret.pld_mars_account.vue.ListAccountsVue;
import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AccountServlet", urlPatterns = {"/api"})
public class AccountServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
        JpaUtil.disableLogs();
        JpaUtil.createPersistenceFactory();
    }

    @Override
    public void destroy() {
        JpaUtil.closePersistenceFactory();
    }

    @Override
    public String getServletInfo() {
        return "Account servlet";
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(JsonServletHelper.ENCODING_UTF8);

        String som = null;

        String pathInfo = request.getPathInfo();
        if (pathInfo != null) som = pathInfo.substring(1);

        String somParameter = request.getParameter("SOM");
        if (somParameter != null) som = somParameter;

        if (som == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing SOM parameter");
            return;
        }

        AccountService accountService = new AccountService();
        try {
            switch (som) {
                case "listAccounts":
                    new ListAccountsAction(accountService).execute(request);
                    new ListAccountsVue().serialize(request, response);
                    break;
                case "addAccount":
                    new AddAccountAction(accountService).execute(request);
                    new AddAccountVue().serialize(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown SOM action : " + som);
                    break;
            }
        } catch (ServiceException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Service exception : " + e.getMessage());
        }
    }
}
