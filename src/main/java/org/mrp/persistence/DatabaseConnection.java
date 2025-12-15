package org.mrp.persistence;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {
    private static Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("application.properties not found");
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void initDatabase() throws SQLException {
        Connection conn = getConnection();

        if (conn != null) {
            System.out.println("Connection established\n");
        }

        try {
            String initStatement = getSQLInitString();
            assert conn != null;
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(initStatement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Database initialized\n");
    }

    private static String getSQLInitString() throws IOException {
        ClassLoader classloader = ClassLoader.getSystemClassLoader();
        URL sqlResource = classloader.getResource("db/init.sql");
        if (sqlResource == null) {
            throw new NullPointerException("sql file not found");
        }
        String filename = sqlResource.getPath();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        StringBuilder initStatement = new StringBuilder();
        while ((line = br.readLine()) != null) {
            initStatement.append(line);
        }
        return initStatement.toString();
    }

    public static Connection getConnection() throws SQLException {
        String username = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        String baseUrl = properties.getProperty("db.base.url");

        if (username == null || password == null || baseUrl == null) {
            throw new SQLException("Database connection parameters are not set.");
        }

        String dbUrl = baseUrl + "?user=" + username + "&password=" + password;

        return DriverManager.getConnection(dbUrl);
    }
}