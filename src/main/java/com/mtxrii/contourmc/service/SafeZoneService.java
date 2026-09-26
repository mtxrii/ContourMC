package com.mtxrii.contourmc.service;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;

public class SafeZoneService {
    private boolean worldGuardEnabled = false;

    public SafeZoneService() {
        Plugin worldGuardPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin != null && worldGuardPlugin.isEnabled()) {
            this.worldGuardEnabled = true;
        }
    }

    public boolean isInSafeZone(Location location) {
        if (!this.worldGuardEnabled) {
            return false;
        }
        
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        
        // Check if PvP is denied in this region
        // testState returns true if the flag is allowed, false if denied or unset
        return !set.testState(null, Flags.PVP);
    }
}
