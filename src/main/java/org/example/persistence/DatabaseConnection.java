package org.example.persistence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    public static void initDatabase() throws SQLException {
        Connection conn = getConnection();

        if (conn != null) {
            System.out.println("Connection established\n");
        }

        try {
            String initStatement = getSQLInitString();
            System.out.println(initStatement);
            assert conn != null;
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(initStatement);
        } catch (Exception e){
            throw new  RuntimeException(e);
        }
        System.out.println("Database initialized\n");
    }

    private static String getSQLInitString() throws IOException {
        ClassLoader classloader = ClassLoader.getSystemClassLoader();
        URL sqlResource = classloader.getResource("db/init.sql");
        if(sqlResource == null){
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
        String dbUrl = "jdbc:postgresql://localhost:5332/postgres?user=postgres&password=password";
        return DriverManager.getConnection(dbUrl);
    }

}
