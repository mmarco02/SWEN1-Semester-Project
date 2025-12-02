package org.example.controllers;
import org.example.service.UserService;

import java.sql.SQLException;
public class UserController {

    UserService userService;

    public UserController(UserService userService) throws SQLException {
        this.userService = userService;
    }
}
