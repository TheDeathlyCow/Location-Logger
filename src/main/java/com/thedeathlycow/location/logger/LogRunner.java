package com.thedeathlycow.location.logger;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class LogRunner extends BukkitRunnable {

    private final LocationDatabase database;

    public LogRunner(LocationDatabase database) {
        this.database = database;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(database::logPlayerLocation);
    }
}