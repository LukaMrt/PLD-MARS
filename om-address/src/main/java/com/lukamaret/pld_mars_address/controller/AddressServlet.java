package com.lukamaret.pld_mars_address.controller;

import com.lukamaret.pld_mars_address.model.AddAddressAction;
import com.lukamaret.pld_mars_address.model.ListAddressesByAccountAction;
import com.lukamaret.pld_mars_address.service.AddressService;
import com.lukamaret.pld_mars_address.utils.JpaUtil;
import com.lukamaret.pld_mars_address.vue.AddAddressVue;
import com.lukamaret.pld_mars_address.vue.ListAddressesByAccountVue;
import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AddressServlet", urlPatterns = {"/api"})
public class AddressServlet extends HttpServlet {
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
        return "Address servlet";
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

        AddressService addressService = new AddressService();
        try {
            switch (som) {
                case "listAddressesByAccount":
                    new ListAddressesByAccountAction(addressService).execute(request);
                    new ListAddressesByAccountVue().serialize(request, response);
                    break;
                case "addAddress":
                    new AddAddressAction(addressService).execute(request);
                    new AddAddressVue().serialize(request, response);
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
