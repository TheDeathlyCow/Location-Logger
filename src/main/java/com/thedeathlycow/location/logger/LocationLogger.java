package com.thedeathlycow.location.logger;

import org.bukkit.plugin.java.JavaPlugin;

public final class LocationLogger extends JavaPlugin {

    private final LocationDatabase database = new LocationDatabase();

    @Override
    public void onEnable() {
        database.open();
    }

    @Override
    public void onDisable() {
        database.close();
    }
}
