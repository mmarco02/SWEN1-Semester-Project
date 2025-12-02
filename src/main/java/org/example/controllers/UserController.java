package org.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.domain.User;
import org.example.persistence.DatabaseConnection;
import org.example.persistence.UserRepository;
import org.example.service.HashUtils;
import org.example.service.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.service.HttpUtils.sendResponse;

public class UserController {

    UserService userService;

    public UserController(UserService userService) throws SQLException {
        this.userService = userService;
    }

    // Router for user paths with variables

}
