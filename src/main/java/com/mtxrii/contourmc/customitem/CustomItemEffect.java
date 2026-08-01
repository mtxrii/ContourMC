package com.mtxrii.contourmc.customitem;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface CustomItemEffect {
    void execute(Location targetLocation, Player user);
}
