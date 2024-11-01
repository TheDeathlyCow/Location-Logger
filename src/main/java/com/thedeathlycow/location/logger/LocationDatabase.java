package com.thedeathlycow.location.logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.joml.Vector3i;

import java.sql.*;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class LocationDatabase implements AutoCloseable {


    private ExecutorService executor;

    private static final Logger LOGGER = Bukkit.getLogger();

    private static final String JDBC_URL = "jdbc:sqlite:locations.db";

    public void open() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            this.initSchema(connection);
        } catch (SQLException e) {
            LOGGER.severe(() -> "Unable to open database: " + e);
        }
    }

    public void logPlayerLocation(Player player) {

        String playerName = player.getName();

        Location location = player.getLocation();
        World world = location.getWorld();

        String worldName = world == null ? null : world.getName();
        Vector3i pos = new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        long time = Instant.now().getEpochSecond();

        executor.submit(() -> {
            this.logValues(playerName, worldName, time, pos);
        });
    }

    @Override
    public void close() {
        executor.close();
    }

    private void logValues(String playerName, String worldName, long timeSeconds, Vector3i position) {

        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            connection.setAutoCommit(false);

            String insertPlayerSql = "INSERT OR IGNORE INTO players (player_name) VALUES (?);";
            String insertWorldSql = "INSERT OR IGNORE INTO worlds (resource_id) VALUES (?);";

            String insertLocationSql = """
                    INSERT INTO locations (player, world, x, y, z, time_seconds)
                    VALUES (
                        (SELECT id from players WHERE player_name = ?),
                        (SELECT id from worlds WHERE resource_id = ?),
                        ?, ?, ?, ?
                    );
                    """;

            try (
                    PreparedStatement insertPlayer = connection.prepareStatement(insertPlayerSql);
                    PreparedStatement insertWorld = connection.prepareStatement(insertWorldSql);
                    PreparedStatement insertLocation = connection.prepareStatement(insertLocationSql)
            ) {
                insertPlayer.setString(1, playerName);
                insertPlayer.executeUpdate();

                insertWorld.setString(1, worldName);
                insertWorld.executeUpdate();

                insertLocation.setString(1, playerName);
                insertLocation.setString(2, worldName);
                insertLocation.setInt(3, position.x);
                insertLocation.setInt(4, position.y);
                insertLocation.setInt(5, position.z);
                insertLocation.setLong(6, timeSeconds);
                insertLocation.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                LOGGER.severe(() -> "Failed to log location: " + e);
                connection.rollback();
            }
        } catch (SQLException e) {
            LOGGER.severe(() -> "Failed to connect to database: " + e);
        }
    }

    private void initSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(30);
            statement.executeUpdate(
                    """
                            CREATE TABLE IF NOT EXISTS worlds (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    resource_id VARCHAR(500) UNIQUE
                            )
                            """
            );
            statement.executeUpdate(
                    """
                            CREATE TABLE IF NOT EXISTS players (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    player_name VARCHAR(500) UNIQUE
                            );
                            """
            );
            statement.executeUpdate(
                    """
                            CREATE TABLE IF NOT EXISTS locations (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    player INTEGER NOT NULL, world INTEGER NOT NULL,
                                    x INTEGER NOT NULL, y INTEGER NOT NULL, z INTEGER NOT NULL,
                                    time_seconds BIGINT NOT NULL,
                                    FOREIGN KEY(player) REFERENCES players(id),
                                    FOREIGN KEY(world) REFERENCES worlds(id)
                            );
                            """
            );
        }
    }
}