package com.lukamaret.pld_mars_address.service;

import com.lukamaret.pld_mars_address.domain.Address;
import com.lukamaret.pld_mars_address.infrastructure.AddressDAO;
import com.lukamaret.pld_mars_address.utils.JpaUtil;

import java.util.List;

public class AddressService {
    public void createAddress(String street, String city, Integer accountId) throws Exception {
        AddressDAO addressDAO = new AddressDAO();
        Address address = new Address(street, city, accountId);

        try {
            JpaUtil.createPersistenceContext();
            JpaUtil.openTransaction();

            addressDAO.create(address);

            JpaUtil.commit();
        } catch (Exception e) {
            JpaUtil.rollback();
            JpaUtil.closePersistenceContext();
            throw e;
        }
        JpaUtil.closePersistenceContext();
    }

    public List<Address> getAddressesByAccountId(Integer accountId) throws Exception {
        AddressDAO addressDAO = new AddressDAO();
        List<Address> addresses;

        try {
            JpaUtil.createPersistenceContext();
            JpaUtil.openTransaction();

            addresses = addressDAO.getByAccountId(accountId);

            JpaUtil.commit();
        } catch (Exception e) {
            JpaUtil.rollback();
            JpaUtil.closePersistenceContext();
            throw e;
        }
        JpaUtil.closePersistenceContext();

        return addresses;
    }
}
