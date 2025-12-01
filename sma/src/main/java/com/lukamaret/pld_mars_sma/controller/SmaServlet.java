package com.lukamaret.pld_mars_sma.controller;

import com.google.gson.Gson;
import com.lukamaret.pld_mars_common.JsonHttpClient;
import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import com.lukamaret.pld_mars_sma.model.AddAccountAction;
import com.lukamaret.pld_mars_sma.model.ListAccountsAction;
import com.lukamaret.pld_mars_sma.service.ServiceMetierApplicatif;
import com.lukamaret.pld_mars_sma.vue.AddAccountVue;
import com.lukamaret.pld_mars_sma.vue.ListAccountsVue;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "SmaServlet", urlPatterns = {"/api"})
public class SmaServlet extends HttpServlet {
    @Override
    public String getServletInfo() {
        return "Servlet SMA";
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(JsonServletHelper.ENCODING_UTF8);

        String sma = null;

        String pathInfo = request.getPathInfo();
        if (pathInfo != null) sma = pathInfo.substring(1);

        String somParameter = request.getParameter("SMA");
        if (somParameter != null) sma = somParameter;

        if (sma == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing SMA action");
            return;
        }

        ServiceMetierApplicatif serviceMetierApplicatif = new ServiceMetierApplicatif(
                System.getProperties().getOrDefault("SOM_ACCOUNT_URL", "http://localhost:8081/som/account").toString(),
                new JsonHttpClient(),
                new Gson()
        );
        try {
            switch (sma) {
                case "listAccounts":
                    new ListAccountsAction(serviceMetierApplicatif).execute(request);
                    new ListAccountsVue().serialize(request, response);
                    break;
                case "addAccount":
                    new AddAccountAction(serviceMetierApplicatif).execute(request);
                    new AddAccountVue().serialize(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown SMA action : " + sma);
                    break;
            }
        } catch (ServiceException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Service exception : " + e.getMessage());
        }
    }
}
