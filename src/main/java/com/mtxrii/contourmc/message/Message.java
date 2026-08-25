package com.mtxrii.contourmc.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Message {
    public static class MultipartMessageArgument {
        private final List<String> arguments;

        private MultipartMessageArgument(List<String> arguments) {
            this.arguments = arguments;
        }
    }

    private static final String TEMPLATE_VAR = "{}";

    private final String message;

    public Message(MessagePrefix prefix, boolean error, String template, String... args) {
        this.message = (error ? "&4&l" : "&3&l") + prefix.getFormatted() + fillOutMessageTemplate(error, template, args);
    }

    public Message(MessagePrefix prefix, boolean error, String template, MultipartMessageArgument... args) {
        this.message = (error ? "&4&l" : "&3&l") + prefix.getFormatted() + fillOutMessageTemplate(error, template, args);
    }

    public Message(MessagePrefix prefix, boolean error, String message) {
        this.message = (error ? "&4&l" : "&3&l") + prefix.getFormatted() + (error ? "&c" : "&b") + message;
    }

    public Message(MessagePrefix prefix, String template, String... args) {
        this(prefix, false, template, args);
    }

    public Message(MessagePrefix prefix, String template, MultipartMessageArgument... args) {
        this.message = "&3&l" + prefix.getFormatted() + fillOutMessageTemplate(false, template, args);
    }

    public Message(MessagePrefix prefix, String message) {
        this(prefix, false, message);
    }

    private Message(String message) {
        this.message = message;
    }

    private static String fillOutMessageTemplate(final boolean error, final String template, final String[] args) {
        int varCount = countMatches(template, TEMPLATE_VAR);
        int argCount = args.length;
        if (varCount != argCount) {
            throw new InputMismatchException(
                    "Template had " + varCount + " variables but " + argCount + " arguments were given"
            );
        }

        String message = (error ? "&c" : "&b") + template;

        for (String arg : args) {
            String coloredArg = "&7" + arg + (error ? "&c" : "&b");
            message = message.replaceFirst(Pattern.quote(TEMPLATE_VAR), coloredArg);
        }
        return message;
    }

    private static String fillOutMessageTemplate(final boolean error, final String template, final MultipartMessageArgument[] args) {
        int varCount = countMatches(template, TEMPLATE_VAR);
        int argCount = args.length;
        if (varCount != argCount) {
            throw new InputMismatchException(
                    "Template had " + varCount + " variables but " + argCount + " arguments were given"
            );
        }

        String msgCol = (error ? "&c" : "&b");
        String message = msgCol + template;

        for (MultipartMessageArgument arg : args) {
            String coloredArg = "&7" + String.join(msgCol + ", &7", arg.arguments) + msgCol;
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
        sender.source().sendMessage(this.getMessageComponent());
    }

    public void sendTo(Player player) {
        player.sendMessage(this.getMessageComponent());
    }

    public void sendTo(HumanEntity humanEntity) {
        humanEntity.sendMessage(this.getMessageComponent());
    }

    public void sendToAll() {
        Bukkit.broadcast(this.getMessageComponent());
    }

    public void sendAsActionBar(Player player) {
        player.sendActionBar(this.getMessageComponent());
    }

    public String getMessage() {
        return this.message;
    }

    public Component getMessageComponent() {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(this.message);
    }

    public Message append(Message message) {
        String messageStr =  this.message + message.getMessage();
        return new Message(messageStr);
    }

    public static MultipartMessageArgument multi(Set<String> arguments) {
        return new MultipartMessageArgument(arguments.stream().toList());
    }

    public static MultipartMessageArgument multi(String[] arguments) {
        return new MultipartMessageArgument(List.of(arguments));
    }
}
