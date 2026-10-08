package com.javarush.bogomolov.axenix.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DbConfig {

    private static final String DEFAULT_URL      = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DEFAULT_USER     = "postgres";
    private static final String DEFAULT_PASSWORD = "postgres";

    private final String url;
    private final String user;
    private final String password;

    private DbConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DbConfig load() {
        Properties props = new Properties();
        try (InputStream in = DbConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("application.properties не найден, использую значения по умолчанию.");
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения application.properties: " + e.getMessage());
        }

        return new DbConfig(
                props.getProperty("db.url",      DEFAULT_URL),
                props.getProperty("db.user",     DEFAULT_USER),
                props.getProperty("db.password", DEFAULT_PASSWORD)
        );
    }

    public String url()      { return url; }
    public String user()     { return user; }
    public String password() { return password; }
}
