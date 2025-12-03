package org.example.http;

import com.sun.net.httpserver.HttpServer;
import org.example.handlers.BaseHandler;
import org.example.handlers.users.UserHandler;
import org.example.handlers.users.UserLoginHandler;
import org.example.handlers.users.UserRegisterHandler;
import org.example.persistence.DatabaseConnection;
import org.example.persistence.UserProfileRepository;
import org.example.persistence.UserRepository;
import org.example.service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

public class Server {
    private HttpServer server;
    private final int port;
    private final UserService userService = new UserService(
            new UserRepository(DatabaseConnection.getConnection()),
            new UserProfileRepository(DatabaseConnection.getConnection())
    );

    public Server(int port) throws SQLException {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Auth routes
        server.createContext("/api/users/register", UserRegisterHandler::handle);
        server.createContext("/api/users/login", UserLoginHandler::handle);

        // User routes with path variables
        server.createContext("/api/users/", UserHandler::handle);

        server.setExecutor(null);
        server.start();

        System.out.println("Server started on port " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server stopped");
        }
    }

    public static void run() throws SQLException, IOException {
        Server server = new Server(8080);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        System.out.println("Press Ctrl+C to stop the server...");
    }
}