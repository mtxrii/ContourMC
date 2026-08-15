package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.Rank;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.RankService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@CommandContainer
public final class EnvironmentCommand {
    private enum ConditionArgument {
        DAY, NIGHT, SUN, RAIN, THUNDER
    }

    private static final long TIME_DAY = 1000;
    private static final long TIME_NIGHT = 13000;
    private static final int WEATHER_DURATION_CLEAR = 0;
    private static final int WEATHER_DURATION_RAIN = 800;

    private static Set<String> conditionArgs = null;

    @Inject private RankService rankService;

    @Command("environment|env <condition>")
    @CommandDescription("Sets the environment of your current world to a specified condition")
    public void environment(
            @NotNull final Source sender,
            @Argument(value = "condition", suggestions = "conditions") final String condition
    ) {
        World targetWorld = Bukkit.getServer().getWorlds().getFirst();
        if (sender instanceof PlayerSource playerSource) {
            Player player = playerSource.source();
            this.rankService.requireRank(MessagePrefix.ENV, Rank.STAFF, player);
            targetWorld = player.getWorld();
        }

        ConditionArgument newCondition;
        try {
            newCondition = ConditionArgument.valueOf(condition.toUpperCase());
        } catch (IllegalArgumentException e) {
            new Message(
                    MessagePrefix.ENV,
                    true,
                    "Invalid condition."
            ).sendTo(sender);
            return;
        }

        switch (newCondition) {
            case DAY -> {
                targetWorld.setTime(TIME_DAY);
                new Message(
                        MessagePrefix.ENV,
                        "Set time in {} to {} ({}).",
                        targetWorld.getName(),
                        String.valueOf(TIME_DAY),
                        convertGameTimeToString(TIME_DAY)
                ).sendTo(sender);
            }
            case NIGHT -> {
                targetWorld.setTime(TIME_NIGHT);
                new Message(
                        MessagePrefix.ENV,
                        "Set time in {} to {} ({}).",
                        targetWorld.getName(),
                        String.valueOf(TIME_NIGHT),
                        convertGameTimeToString(TIME_NIGHT)
                ).sendTo(sender);
            }
            case SUN -> {
                targetWorld.setStorm(false);
                targetWorld.setThundering(false);
                targetWorld.setWeatherDuration(WEATHER_DURATION_CLEAR);
                new Message(
                        MessagePrefix.ENV,
                        "Set weather in {} to clear.",
                        targetWorld.getName()
                ).sendTo(sender);
            }
            case RAIN -> {
                targetWorld.setStorm(true);
                targetWorld.setWeatherDuration(WEATHER_DURATION_RAIN);
                new Message(
                        MessagePrefix.ENV,
                        "Set weather in {} to rain.",
                        targetWorld.getName()
                ).sendTo(sender);
            }
            case THUNDER -> {
                targetWorld.setStorm(true);
                targetWorld.setThundering(true);
                targetWorld.setThunderDuration(WEATHER_DURATION_RAIN);
                new Message(
                        MessagePrefix.ENV,
                        "Set weather in {} to thunder.",
                        targetWorld.getName()
                ).sendTo(sender);
            }
        }
    }

    @Suggestions("conditions")
    public @NotNull Set<String> suggestKit(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        if (conditionArgs == null) {
            conditionArgs = Arrays.stream(ConditionArgument.values())
                                  .map(ca -> ca.name().toLowerCase())
                                  .collect(Collectors.toSet());
        }
        return conditionArgs;
    }

    private static String convertGameTimeToString(long ticks) {
        if (ticks < 0 || ticks > 24000) {
            return null;
        }

        int totalMinecraftHours = 24;
        int ticksPerHour = 1000;
        double ticksPerMinute = ticksPerHour / 60.0;

        int hours = ((int) ((ticks / ticksPerHour) % totalMinecraftHours) + 6) % 24;
        int minutes = (int)((ticks % ticksPerHour) / ticksPerMinute);

        return String.format("%02d:%02d", hours, minutes);
    }
}
