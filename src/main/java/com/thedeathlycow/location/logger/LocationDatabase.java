package com.thedeathlycow.location.logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.joml.Vector3i;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class LocationDatabase implements AutoCloseable {


    private ExecutorService executor;

    private static final Logger LOGGER = Bukkit.getLogger();

    private static final String JDBC_URL = "jdbc:sqlite:locations.db";

    private static final String INSERT_PLAYER_SQL = "INSERT OR IGNORE INTO players (player_name) VALUES (?);";
    private static final String INSERT_WORLD_SQL = "INSERT OR IGNORE INTO worlds (resource_id) VALUES (?);";
    private static final String INSERT_LOCATION_SQL = """
            INSERT INTO locations (player, world, x, y, z, time_seconds)
            VALUES (
                (SELECT id from players WHERE player_name = ?),
                (SELECT id from worlds WHERE resource_id = ?),
                ?, ?, ?, ?
            );
            """;


    public void open() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            this.initSchema(connection);
        } catch (SQLException e) {
            LOGGER.severe(() -> "Unable to open database: " + e);
        }
    }

    public void logPlayerLocation(Player player, long time) {
        String playerName = player.getName();

        Location location = player.getLocation();
        World world = location.getWorld();

        String worldName = world == null ? "null" : world.getName();
        Vector3i pos = new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        this.executor.submit(() -> logValues(playerName, worldName, pos, time));
    }

    @Override
    public void close() {
        this.executor.close();
    }

    private static void logValues(String playerName, String worldName, Vector3i position, long timeSeconds) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            connection.setAutoCommit(false);
            insertValuesWithConnection(playerName, worldName, position, timeSeconds, connection);
        } catch (SQLException e) {
            LOGGER.severe(() -> "Failed to connect to database: " + e);
        }
    }

    private static void insertValuesWithConnection(
            String playerName,
            String worldName,
            Vector3i position,
            long timeSeconds,
            Connection connection
    ) throws SQLException {
        try (
                PreparedStatement insertPlayer = connection.prepareStatement(INSERT_PLAYER_SQL);
                PreparedStatement insertWorld = connection.prepareStatement(INSERT_WORLD_SQL);
                PreparedStatement insertLocation = connection.prepareStatement(INSERT_LOCATION_SQL)
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