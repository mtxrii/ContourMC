package com.mtxrii.contourmc.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.InputMismatchException;
import java.util.regex.Pattern;

public class Message {
    private static final String TEMPLATE_VAR = "{}";

    private final String message;

    public Message(MessagePrefix prefix, boolean error, String template, String... args) {
        this.message = (error ? "&4&l" : "&3&l") + prefix.getFormatted() + fillOutMessageTemplate(error, template, args);
    }

    public Message(MessagePrefix prefix, String template, String... args) {
        this(prefix, false, template, args);
    }

    private static String fillOutMessageTemplate(final boolean error, final String template, final String[] args) {
        int varCount = countMatches(template, TEMPLATE_VAR);
        int argCount = args.length;
        if (varCount != argCount) {
            throw new InputMismatchException(
                    "Template had " + varCount + " variables but " + argCount + " arguments were given"
            );
        }

        String message = template;
        if (error) {
            message = "&c" + message;
        } else {
            message = "&b" + message;
        }

        for (String arg : args) {
            String coloredArg = "&7" + arg + (error ? "&c" : "&b");
            message = message.replaceFirst(Pattern.quote(TEMPLATE_VAR), coloredArg);
        }
        return message;
    }

    private static int countMatches(final CharSequence str, final CharSequence sub) {
        if (str.isEmpty() || sub.isEmpty()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = str.toString().indexOf(sub.toString(), idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    public void sendTo(Source sender) {
        Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize(this.message);
        sender.source().sendMessage(msg);
    }

    public void sendTo(Player player) {
        Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize(this.message);
        player.sendMessage(msg);
    }

    public void sendTo(HumanEntity humanEntity) {
        Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize(this.message);
        humanEntity.sendMessage(msg);
    }

    public String getMessage() {
        return this.message;
    }
}
