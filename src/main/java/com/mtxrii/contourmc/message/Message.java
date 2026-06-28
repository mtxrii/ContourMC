package com.mtxrii.contourmc.message;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.InputMismatchException;
import java.util.regex.Pattern;

public class Message {
    private static final String TEMPLATE_VAR = "{}";

    private final MessagePrefix prefix;
    private final String message;
    private final boolean error;

    public Message(MessagePrefix prefix, boolean error, String template, String... args) {
        this.prefix = prefix;
        this.message = prefix.getFormatted() + fillOutMessageTemplate(template, args);
        this.error = error;
    }

    public Message(MessagePrefix prefix, String template, String... args) {
        this.prefix = prefix;
        this.message = prefix.getFormatted() + fillOutMessageTemplate(template, args);
        this.error = false;
    }

    private static String fillOutMessageTemplate(final String template, final String[] args) {
        int varCount = countMatches(template, TEMPLATE_VAR);
        int argCount = args.length;
        if (varCount != argCount) {
            throw new InputMismatchException(
                    "Template had " + varCount + " variables but " + argCount + " arguments were given"
            );
        }

        String message = template;
        for (String arg : args) {
            message = message.replaceFirst(Pattern.quote(TEMPLATE_VAR), arg);
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
        sender.source().sendMessage(this.message);
    }

    public void sendTo(Player player) {
        player.sendMessage(this.message);
    }

    public void sendTo(HumanEntity humanEntity) {
        humanEntity.sendMessage(this.message);
    }

    public String getMessage() {
        return this.message;
    }
}
