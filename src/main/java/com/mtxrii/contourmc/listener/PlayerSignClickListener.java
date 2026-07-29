package com.mtxrii.contourmc.listener;

import com.google.inject.Inject;
import com.mtxrii.contourmc.service.KitService;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/// On player right-click sign block:
/// - If text matches kit sign:
///   - Equip kit
@Component
public class PlayerSignClickListener implements Listener {
    private Plugin plugin;
    private KitService kitService;

    @Inject
    public PlayerSignClickListener(Plugin plugin, KitService kitService) {
        this.plugin = plugin;
        this.kitService = kitService;
    }
}
