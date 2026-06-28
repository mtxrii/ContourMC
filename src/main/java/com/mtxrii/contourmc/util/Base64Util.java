package com.mtxrii.contourmc.util;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@UtilityClass
public final class Base64Util {

    public static String inventoryToBase64(Inventory inventory) throws IOException {
        try (
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)
        ) {
            dataOutput.writeInt(inventory.getSize());
            for (ItemStack item : inventory.getContents()) {
                dataOutput.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }

    public static ItemStack[] inventoryFromBase64(String data) throws IOException, ClassNotFoundException {
        try (
                ByteArrayInputStream dataInput = new ByteArrayInputStream(Base64.getDecoder().decode(data));
                BukkitObjectInputStream input = new BukkitObjectInputStream(dataInput)
        ) {
            int size = input.readInt();
            ItemStack[] contents = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                contents[i] = (ItemStack) input.readObject();
            }
            input.close();
            return contents;
        }
    }
}
