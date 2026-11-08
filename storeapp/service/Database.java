package com.example.storeapp.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;

public class Database {

    private static final String URL  = "jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres";
    private static final String USER = "postgres.bfugkzkzdeblwswxiruk";
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    private static final HikariDataSource DATA_SOURCE;

    static {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(URL);
        cfg.setUsername(USER);
        cfg.setPassword(PASSWORD);

        cfg.setMaximumPoolSize(10);      // до 10 одновременных соединений
        cfg.setMinimumIdle(2);           // 2 всегда «горячие»
        cfg.setConnectionTimeout(5000);
        cfg.setIdleTimeout(60_000);
        cfg.setMaxLifetime(1_800_000);   // 30 минут
        cfg.setPoolName("pelegood-pool");

        DATA_SOURCE = new HikariDataSource(cfg);

        // Закрыть пул при выходе
        Runtime.getRuntime().addShutdownHook(new Thread(DATA_SOURCE::close));
    }

    /** Возвращает соединение из пула. Закрытие возвращает его в пул, а не закрывает TCP. */
    public static Connection connect() throws Exception {
        return DATA_SOURCE.getConnection();

    }
}