package com.mtxrii.contourmc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public enum EffectSign {
    TEST("[test]", "&8&l[&7Test&8&l]"),
    HEAL("[heal]", "&8&l[&7Heal&8&l]"),
    SPAWN("[spawn]", "&8&l[&7Spawn&8&l]"),
    KIT("[kit]", "&8&l[&7Kit&8&l]");

    private final String inputSignText;
    private final String formattedSignText;

    EffectSign(String inputSignText, String formattedSignText) {
        this.inputSignText = inputSignText;
        this.formattedSignText = formattedSignText;
    }

    public Component getFormattedSignTextComponent() {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(this.formattedSignText);
    }

    public static EffectSign getSignFromInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        for (EffectSign sign : EffectSign.values()) {
            if (input.equalsIgnoreCase(sign.inputSignText)) {
                return sign;
            }
        }
        return null;
    }

    public static EffectSign getSignFromFormatted(String signText) {
        if (signText == null || signText.trim().isEmpty()) {
            return null;
        }
        signText = signText.replace('§', '&'); // Alternatively run formatted text thru colorize method

        for (EffectSign sign : EffectSign.values()) {
            if (signText.equalsIgnoreCase(sign.formattedSignText)) {
                return sign;
            }
        }
        return null;
    }
}
