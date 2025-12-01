package com.lukamaret.pld_mars_account.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DASI Team
 */
public class JpaUtil {
    public static final String PERSISTENCE_UNIT_NAME = "com.lukamaret.pld_mars_sma";

    private static EntityManagerFactory entityManagerFactory = null;

    private static final ThreadLocal<EntityManager> threadLocalEntityManager = ThreadLocal.withInitial(() -> null);

    private static boolean JPAUTIL_LOG_ACTIVE = true;

    private static void log(String message) {
        if (JPAUTIL_LOG_ACTIVE) {
            System.out.println("[JpaUtil:Log] " + message);
        }
    }

    public static void disableLogs() {
        JPAUTIL_LOG_ACTIVE = false;
    }

    public static synchronized void createPersistenceFactory() {
        log("Création de la fabrique de contexte de persistance");
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
        Map<String, String> propertyMap = new HashMap<>();
        if (!JPAUTIL_LOG_ACTIVE) {
            propertyMap.put("eclipselink.logging.level", "OFF");
        }
        String url = System.getProperties().getOrDefault("DATABASE_URL", "jdbc:mysql://localhost:3306/pld_mars?serverTimezone=UTC").toString();
        String username = System.getProperties().getOrDefault("DATABASE_USER", "root").toString();
        String password = System.getProperties().getOrDefault("DATABASE_PASSWORD", "").toString();
        propertyMap.put("jakarta.persistence.jdbc.url", url);
        propertyMap.put("jakarta.persistence.jdbc.user", username);
        propertyMap.put("jakarta.persistence.jdbc.password", password);
        entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, propertyMap);
    }

    public static synchronized void closePersistenceFactory() {
        log("Fermeture de la fabrique de contexte de persistance");
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }

    public static void createPersistenceContext() {
        log("Création du contexte de persistance");
        threadLocalEntityManager.set(entityManagerFactory.createEntityManager());
    }

    public static void closePersistenceContext() {
        log("Fermeture du contexte de persistance");
        EntityManager em = threadLocalEntityManager.get();
        em.close();
        threadLocalEntityManager.remove();
    }

    public static void openTransaction() {
        log("Ouverture de la transaction (begin)");
        try {
            EntityManager em = threadLocalEntityManager.get();
            em.getTransaction().begin();
        } catch (Exception ex) {
            log("Erreur lors de l'ouverture de la transaction");
            throw ex;
        }
    }

    public static void commit() {
        log("Validation de la transaction (commit)");
        try {
            EntityManager em = threadLocalEntityManager.get();
            em.getTransaction().commit();
        } catch (Exception ex) {
            log("Erreur lors de la validation (commit) de la transaction");
            throw ex;
        }
    }

    public static void rollback() {
        try {
            log("Annulation de la transaction (rollback)");

            EntityManager em = threadLocalEntityManager.get();
            if (em.getTransaction().isActive()) {
                log("Annulation effective de la transaction (rollback d'une transaction active)");
                em.getTransaction().rollback();
            }

        } catch (Exception ex) {
            log("Erreur lors de l'annulation (rollback) de la transaction");
        }
    }

    public static EntityManager getPersistenceFactory() {
        log("Obtention du contexte de persistance");
        return threadLocalEntityManager.get();
    }
}
