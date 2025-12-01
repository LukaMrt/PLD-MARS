package com.lukamaret.pld_mars_ihm.controller;

import com.lukamaret.pld_mars_sma.service.AccountService;
import com.lukamaret.pld_mars_sma.utils.JpaUtil;
import com.lukamaret.pld_mars_ihm.model.AddAccountAction;
import com.lukamaret.pld_mars_ihm.model.ListAccountsAction;
import com.lukamaret.pld_mars_ihm.vue.AddAccountVue;
import com.lukamaret.pld_mars_ihm.vue.ListAccountsVue;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "MainServlet", urlPatterns = {"/api"})
public class MainServlet extends HttpServlet {
    @Override
    public void init() {
        JpaUtil.disableLogs();
        JpaUtil.createPersistenceFactory();
    }

    @Override
    public void destroy() {
        JpaUtil.closePersistenceFactory();
    }

    @Override
    public String getServletInfo() {
        return "Default servlet for handling actions";
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AccountService accountService = new AccountService();

        String actionType = request.getParameter("actionType");
        switch (actionType) {
            case "listAccounts":
                new ListAccountsAction(accountService).execute(request);
                new ListAccountsVue().serialize(request, response);
                break;
            case "addAccount":
                new AddAccountAction(accountService).execute(request);
                new AddAccountVue().serialize(request, response);
                break;
            default:
                throw new IllegalArgumentException("Unknown action type: " + actionType);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }
}
