package com.lukamaret.pld_mars_account.infrastructure;

import com.lukamaret.pld_mars_account.domain.Account;
import com.lukamaret.pld_mars_account.utils.JpaUtil;

import java.util.List;

public class AccountDAO {
    public void create(Account account) {
        JpaUtil.getPersistenceFactory()
                .persist(account);
    }

    public List<Account> getAll() {
        return JpaUtil.getPersistenceFactory()
                .createQuery("SELECT a FROM Account a", Account.class)
                .getResultList();
    }
}
