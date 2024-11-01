package com.thedeathlycow.location.logger;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class LocationDatabase implements AutoCloseable {

    private Connection connection;

    private static final Logger LOGGER = Bukkit.getLogger();

    public void open() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
        } catch (SQLException e) {
            LOGGER.severe(() -> "Unable to open database: " + e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.severe(() -> "Unable to close database: " + e);
        }
    }
}