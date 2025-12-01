package com.lukamaret.pld_mars_account.console;

import com.lukamaret.pld_mars_account.domain.Account;
import com.lukamaret.pld_mars_account.service.AccountService;
import com.lukamaret.pld_mars_account.utils.JpaUtil;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;

public class Main {
    static void main() {
        Dotenv.load()
                .entries()
                .forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        System.out.println("🚀 Initializing JPA...");
        JpaUtil.disableLogs();
        JpaUtil.createPersistenceFactory();

        AccountService service = new AccountService();
        try {
            System.out.println("📝 Creating account 'Luka'...");
            service.createAccount("Luka");
            System.out.println("✅ Account created successfully!");

            System.out.println("\n📋 Fetching all accounts...");
            List<Account> accounts = service.getAllAccounts();
            System.out.println("Found " + accounts.size() + " account(s):\n");

            for (Account account : accounts) {
                System.out.println("  👤 Account: " + account.getId() + " - " + account.getName());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }

        System.out.println("\n👋 Closing JPA...");
        JpaUtil.closePersistenceFactory();
        System.out.println("✨ Done!");
    }
}
