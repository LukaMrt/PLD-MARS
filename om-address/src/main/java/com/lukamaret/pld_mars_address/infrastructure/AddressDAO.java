package com.lukamaret.pld_mars_address.infrastructure;

import com.lukamaret.pld_mars_address.domain.Address;
import com.lukamaret.pld_mars_address.utils.JpaUtil;

import java.util.List;

public class AddressDAO {
    public void create(Address address) {
        JpaUtil.getPersistenceFactory()
                .persist(address);
    }

    public List<Address> getByAccountId(Integer accountId) {
        return JpaUtil.getPersistenceFactory()
                .createQuery("SELECT a FROM Address a WHERE a.accountId = :accountId", Address.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }
}
