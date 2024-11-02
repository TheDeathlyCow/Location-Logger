package com.thedeathlycow.location.logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class LocationLoggerPlugin extends JavaPlugin {

    private final LocationDatabase database = new LocationDatabase();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        database.open(this.getJdbcUrl());

        BukkitRunnable updateListener = new LogRunner(this.database);
        long intervalTicks = this.getLogIntervalTicks();
        updateListener.runTaskTimer(this, intervalTicks, intervalTicks);
    }

    @Override
    public void onDisable() {
        database.close();
    }

    public String getJdbcUrl() {
        return "jdbc:sqlite:" + this.getConfig().getString("database-path");
    }

    public long getLogIntervalTicks() {
        return this.getConfig().getLong("log-interval-seconds") * 20L;
    }
}
