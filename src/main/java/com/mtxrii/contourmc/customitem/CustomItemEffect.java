package com.mtxrii.contourmc.customitem;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface CustomItemEffect {
    boolean execute(Location targetLocation, Player user);
}
