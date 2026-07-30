package com.mtxrii.contourmc.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@UtilityClass
public final class ItemUtil {

    public static boolean hasExactPlainName(ItemStack item, String expectedName) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        Component displayName = meta.displayName();

        if (displayName == null) {
            return false;
        }

        String plain = PlainTextComponentSerializer.plainText().serialize(displayName);
        if (!plain.equals(expectedName)) {
            return false;
        }

        return isPlain(displayName);
    }

    private static boolean isPlain(Component component) {
        Style style = component.style();

        // Vanilla anvil names don't explicitly disable italics.
        if (style.decoration(TextDecoration.ITALIC) != TextDecoration.State.FALSE) {
            return false;
        }

        // Reject any color
        if (style.color() != null) {
            return false;
        }

        // Reject any other enabled decorations
        for (TextDecoration decoration : TextDecoration.values()) {
            if (decoration == TextDecoration.ITALIC) {
                continue;
            }

            if (style.decoration(decoration) == TextDecoration.State.TRUE) {
                return false;
            }
        }

        // Reject other formatting/features
        if (style.font() != null
                || style.clickEvent() != null
                || style.hoverEvent() != null
                || style.insertion() != null) {
            return false;
        }

        // Check children recursively
        for (Component child : component.children()) {
            if (!isPlain(child)) {
                return false;
            }
        }

        return true;
    }
}
