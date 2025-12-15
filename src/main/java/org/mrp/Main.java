package org.mrp;

import org.mrp.http.Server;
import org.mrp.persistence.DatabaseConnection;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    static void main() throws SQLException, IOException {
        DatabaseConnection.initDatabase();
        Server.run();
    }
}
