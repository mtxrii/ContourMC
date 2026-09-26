package com.mtxrii.contourmc.service;

import com.google.inject.Singleton;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import com.sxtanna.platform.archetype.Component;

@Component
@Singleton
public class SafeZoneService {
    public boolean isInSafeZone(Location location) {
        Plugin worldGuardPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin == null || !worldGuardPlugin.isEnabled()) {
            return false;
        }
        
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

        // queryState returns State.DENY, State.ALLOW, or null if unset.
        // Only block PvP if it is explicitly denied.
        return set.queryState(null, Flags.PVP) == StateFlag.State.DENY;
    }
}
