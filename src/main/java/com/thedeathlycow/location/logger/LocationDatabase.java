package com.thedeathlycow.location.logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class LocationDatabase implements AutoCloseable {

    private Connection connection;

    private ExecutorService executor;

    private static final Logger LOGGER = Bukkit.getLogger();

    public void open() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            this.initSchema();
        } catch (SQLException e) {
            LOGGER.severe(() -> "Unable to open database: " + e);
        }
    }

    public void logPlayerLocation(Player player) {
    }

    @Override
    public void close() {
        executor.close();
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.severe(() -> "Unable to close database: " + e);
        }
    }

    private void initSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(30);
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS worlds (id INTEGER PRIMARY KEY AUTOINCREMENT, resource_id VARCHAR(500) UNIQUE)"
            );
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS players (id INTEGER PRIMARY KEY AUTOINCREMENT, player_name VARCHAR(500))"
            );
            statement.executeUpdate(
                    """
                                CREATE TABLE IF NOT EXISTS locations (
                                        player INTEGER NOT NULL, world INTEGER NOT NULL,
                                        x INTEGER NOT NULL, y INTEGER NOT NULL, z INTEGER NOT NULL,
                                        FOREIGN KEY(player) REFERENCES players(id),
                                        FOREIGN KEY(world) REFERENCES worlds(id),
                                        PRIMARY KEY (player, world, x, y, z)
                                );
                            """
            );
        }
    }
}