package org.example;

import org.example.http.Server;
import org.example.persistence.DatabaseConnection;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class Main {
    static void main() throws NoSuchAlgorithmException, SQLException, IOException {
        DatabaseConnection.initDatabase();

        Server.run();
    }
}
