package com.lukamaret.pld_mars_address.model;

import com.google.gson.JsonObject;
import com.lukamaret.pld_mars_address.service.AddressService;
import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;

public class AddAddressAction extends Action {
    private final AddressService service;

    public AddAddressAction(AddressService service) {
        this.service = service;
    }

    @Override
    public void execute(HttpServletRequest request) throws ServiceException {
        String street;
        String city;
        Integer accountId;

        JsonObject body = getBody(request);
        if (body == null) {
            street = request.getParameter("street");
            city = request.getParameter("city");
            accountId = Integer.parseInt(request.getParameter("accountId"));
        } else {
            street = body.get("street").getAsString();
            city = body.get("city").getAsString();
            accountId = body.get("accountId").getAsInt();
        }

        try {
            service.createAddress(street, city, accountId);
        } catch (Exception e) {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("address", "addAddress", e);
        }

        request.setAttribute("code", 200);
    }
}
