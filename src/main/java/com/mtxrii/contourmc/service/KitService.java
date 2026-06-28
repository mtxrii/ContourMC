package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.mtxrii.contourmc.config.KitsConfiguration;
import com.mtxrii.contourmc.exception.KitArgumentException;
import com.mtxrii.contourmc.util.Base64Util;
import com.sxtanna.platform.archetype.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.io.IOException;
import java.util.Set;

@Component
public class KitService {
    private final JacksonConfigurationLoader configLoader;
    private final KitsConfiguration kitsConfig;

    @Inject
    public KitService(Plugin plugin) {
        this.configLoader = JacksonConfigurationLoader.builder()
                .path(plugin.getDataPath().resolve("kits.json"))
                .build();
        try {
            this.kitsConfig = this.configLoader.load().get(KitsConfiguration.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> kitNames() {
        return this.kitsConfig.kits.keySet();
    }

    public void equipKit(
            @NotNull String kitName,
            @NotNull HumanEntity playerEntity
    ) throws KitArgumentException {
        String kitBase64 = this.kitsConfig.kits.get(kitName);
        if (kitBase64 == null) {
            throw new KitArgumentException("Kit not found");
        }

        ItemStack[] kitContents;
        try {
            kitContents = Base64Util.inventoryFromBase64(kitBase64);
        } catch (IOException | ClassNotFoundException e) {
            playerEntity.sendMessage("[Error] kits are momentarily unavailable. Please try again later.");
            throw new RuntimeException(e);
        }

        for (int i = 0; i < kitContents.length; i++) {
            ItemStack kitItemStack = kitContents[i] != null ? kitContents[i] : new ItemStack(Material.AIR);
            playerEntity.getInventory().setItem(i, kitItemStack);
        }
    }

    public void saveNewKit(
            @NotNull String kitName,
            @NotNull PlayerInventory kitHolderInventory
    ) throws KitArgumentException {
        if (this.kitsConfig.kits.containsKey(kitName)) {
            throw new KitArgumentException(
                    "Kit already exists with this name. Either delete it first or choose a different name."
            );
        }

        String kitBase64;
        try {
            kitBase64 = Base64Util.inventoryToBase64(kitHolderInventory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.kitsConfig.kits.put(kitName, kitBase64);
        this.saveConfig();
    }

    public void deleteKit(
            @NotNull String kitName
    ) throws KitArgumentException {
        if (!this.kitsConfig.kits.containsKey(kitName)) {
            throw new KitArgumentException("No kit exists with this name.");
        }

        this.kitsConfig.kits.remove(kitName);
        this.saveConfig();
    }

    private void saveConfig() {
        try {
            this.configLoader.save(this.configLoader.createNode().set(this.kitsConfig));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}