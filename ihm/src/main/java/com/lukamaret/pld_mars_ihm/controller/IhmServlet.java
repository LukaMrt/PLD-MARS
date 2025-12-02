package com.lukamaret.pld_mars_ihm.controller;

import com.google.gson.Gson;
import com.lukamaret.pld_mars_common.JsonHttpClient;
import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import com.lukamaret.pld_mars_ihm.model.AddAccountAction;
import com.lukamaret.pld_mars_ihm.model.AddAddressAction;
import com.lukamaret.pld_mars_ihm.model.ListAccountsAction;
import com.lukamaret.pld_mars_ihm.service.IhmService;
import com.lukamaret.pld_mars_ihm.vue.AddAccountVue;
import com.lukamaret.pld_mars_ihm.vue.AddAddressVue;
import com.lukamaret.pld_mars_ihm.vue.ListAccountsVue;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "IhmServlet", urlPatterns = {"/api"})
public class IhmServlet extends HttpServlet {
    @Override
    public String getServletInfo() {
        return "Default servlet for handling actions";
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(JsonServletHelper.ENCODING_UTF8);
        IhmService ihmService = new IhmService(
            System.getProperties().getOrDefault("SMA_URL", "http://localhost:8080/sma").toString(),
            new JsonHttpClient(),
            new Gson()
        );

        String action = request.getParameter("action");
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing action parameter");
            return;
        }

        try {
            switch (action) {
                case "listAccounts":
                    new ListAccountsAction(ihmService).execute(request);
                    new ListAccountsVue().serialize(request, response);
                    break;
                case "addAccount":
                    new AddAccountAction(ihmService).execute(request);
                    new AddAccountVue().serialize(request, response);
                    break;
                case "addAddress":
                    new AddAddressAction(ihmService).execute(request);
                    new AddAddressVue().serialize(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown IHM action : " + action);
                    break;
            }
        } catch (ServiceException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Service exception : " + e.getMessage());
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
