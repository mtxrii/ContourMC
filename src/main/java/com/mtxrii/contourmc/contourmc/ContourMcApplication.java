package com.mtxrii.contourmc.contourmc;

import org.bukkit.plugin.java.JavaPlugin;

public class ContourMcApplication extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Contour MC is enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Contour MC is disabled");
    }

}
