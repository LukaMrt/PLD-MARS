package com.lukamaret.pld_mars_account.service;

import com.lukamaret.pld_mars_account.infrastructure.AccountDAO;
import com.lukamaret.pld_mars_account.domain.Account;
import com.lukamaret.pld_mars_account.utils.JpaUtil;

import java.util.List;

public class AccountService {
    public List<Account> getAllAccounts() throws Exception {
        AccountDAO accountDAO = new AccountDAO();
        List<Account> accounts;

        try {
            JpaUtil.createPersistenceContext();
            JpaUtil.openTransaction();

            accounts = accountDAO.getAll();

            JpaUtil.commit();
        } catch (Exception e) {
            JpaUtil.rollback();
            JpaUtil.closePersistenceContext();
            throw e;
        }
        JpaUtil.closePersistenceContext();

        return accounts;
    }

    public void createAccount(String name) throws Exception {
        AccountDAO accountDAO = new AccountDAO();
        Account account = new Account(name);

        try {
            JpaUtil.createPersistenceContext();
            JpaUtil.openTransaction();

            accountDAO.create(account);

            JpaUtil.commit();
        } catch (Exception e) {
            JpaUtil.rollback();
            JpaUtil.closePersistenceContext();
            throw e;
        }
        JpaUtil.closePersistenceContext();
    }
}
