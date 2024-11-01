package com.thedeathlycow.location.logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

public final class LocationLogger extends JavaPlugin {

    private final LocationDatabase database = new LocationDatabase();

    private BukkitRunnable updateListener;

    private static final long SECONDS_PER_LOG = 5L;

    @Override
    public void onEnable() {
        database.open();

        updateListener = new LogRunner(this.database);
        updateListener.runTaskTimer(this, SECONDS_PER_LOG * 20L, SECONDS_PER_LOG * 20L);
    }

    @Override
    public void onDisable() {
        database.close();
    }
}
