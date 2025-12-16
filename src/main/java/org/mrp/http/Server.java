package org.mrp.http;

import com.sun.net.httpserver.HttpServer;
import org.mrp.handlers.mediaEntries.MediaEntryHandler;
import org.mrp.handlers.ratings.RatingsHandler;
import org.mrp.handlers.users.UserHandler;
import org.mrp.handlers.users.UserLoginHandler;
import org.mrp.handlers.users.UserRegisterHandler;
import org.mrp.persistence.DatabaseConnection;
import org.mrp.persistence.implemenatations.*;
import org.mrp.service.MediaService;
import org.mrp.service.RatingService;
import org.mrp.service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;

public class Server {
    private HttpServer server;
    private final int port;
    private final UserService userService;
    private final MediaService mediaService;
    private final RatingService ratingService;

    public Server(int port) throws SQLException {
        this.port = port;

        Connection connection = DatabaseConnection.getConnection();

        UserRepository userRepository = new UserRepository(connection);
        UserProfileRepository userProfileRepository = new UserProfileRepository(connection);
        TokenRepository tokenRepository = new TokenRepository(connection);
        MediaEntryRepository mediaEntryRepository = new MediaEntryRepository(connection);
        RatingRepository ratingRepository = new RatingRepository(connection);
        LikeRepository likeRepository = new LikeRepository(connection);

        this.ratingService = new RatingService(ratingRepository, likeRepository);
        this.userService = new UserService(userRepository, userProfileRepository, tokenRepository);
        this.mediaService = new MediaService(mediaEntryRepository, ratingRepository);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Auth routes
        server.createContext("/api/users/register", UserRegisterHandler::handle);
        server.createContext("/api/users/login", UserLoginHandler::handle);

        // User routes
        server.createContext("/api/users/", UserHandler::handle);

        // MediaEntry routes
        server.createContext("/api/media", MediaEntryHandler::handle);

        // Rating routes
        server.createContext("/api/ratings/", RatingsHandler::handle);

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
    }

    public boolean isRunning() {
        //check if server is initialized
        return server != null;
    }
}