package com.thedeathlycow.location.logger;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;

public class LogRunner extends BukkitRunnable {

    private final LocationDatabase database;

    public LogRunner(LocationDatabase database) {
        this.database = database;
    }

    @Override
    public void run() {
        long time = Instant.now().getEpochSecond();
        Bukkit.getOnlinePlayers().forEach(player -> database.logPlayerLocation(player, time));
    }
}