package com.thedeathlycow.location.logger;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogRunner extends BukkitRunnable {

    private final LocationDatabase database;

    private static final Logger LOGGER = Bukkit.getLogger();

    public LogRunner(LocationDatabase database) {
        this.database = database;
    }

    @Override
    public void run() {
//        LOGGER.info(() -> "Logging player locations");
        Bukkit.getOnlinePlayers().forEach(database::logPlayerLocation);
    }
}