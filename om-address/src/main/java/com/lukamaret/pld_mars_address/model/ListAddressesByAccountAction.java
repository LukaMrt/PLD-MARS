package com.lukamaret.pld_mars_address.model;

import com.lukamaret.pld_mars_address.domain.Address;
import com.lukamaret.pld_mars_address.service.AddressService;
import com.lukamaret.pld_mars_common.JsonServletHelper;
import com.lukamaret.pld_mars_common.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public class ListAddressesByAccountAction extends Action {
    private final AddressService service;

    public ListAddressesByAccountAction(AddressService service) {
        this.service = service;
    }

    @Override
    public void execute(HttpServletRequest request) throws ServiceException {
        Integer accountId = Integer.parseInt(request.getParameter("accountId"));
        List<Address> addresses;

        try {
            addresses = service.getAddressesByAccountId(accountId);
        } catch (Exception e) {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("address", "listAddressesByAccount", e);
        }

        request.setAttribute("code", 200);
        request.setAttribute("addresses", addresses);
    }
}
