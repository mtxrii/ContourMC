package com.mtxrii.contourmc.command;

import com.google.inject.Inject;
import com.mtxrii.contourmc.config.PlayerStatsConfiguration;
import com.mtxrii.contourmc.message.Message;
import com.mtxrii.contourmc.message.MessagePrefix;
import com.mtxrii.contourmc.service.PlayerStatsService;
import org.bukkit.Bukkit;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToIntFunction;

@CommandContainer
public final class TopCommand {
    private static final int MAX_ENTRIES = 10;

    @Inject private PlayerStatsService playerStatsService;

    @Command("top [category]")
    @CommandDescription("Shows the PvP leaderboard")
    public void top(
            @NotNull final Source sender,
            @Argument(value = "category", suggestions = "topCategories") final String category
    ) {
        String normalizedCategory = category == null ? null : category.toLowerCase();
        if (normalizedCategory != null && !isSupportedCategory(normalizedCategory)) {
            new Message(
                    MessagePrefix.INFO,
                    true,
                    "Unknown leaderboard. Choose kills, deaths, or killstreak."
            ).sendTo(sender);
            return;
        }

        if (normalizedCategory == null) {
            new Message(
                    MessagePrefix.INFO,
                    "Top leaderboards:\n\n{}\n\n{}\n\n{}",
                    this.formatLeaderboard("Kills", stats -> stats.kills),
                    this.formatLeaderboard("Deaths", stats -> stats.deaths),
                    this.formatLeaderboard("Killstreak", stats -> stats.killstreak)
            ).sendTo(sender);
            return;
        }

        String title = switch (normalizedCategory) {
            case "kills" -> "Kills";
            case "deaths" -> "Deaths";
            case "killstreak" -> "Killstreak";
            default -> throw new IllegalStateException("Unsupported leaderboard: " + normalizedCategory);
        };
        ToIntFunction<PlayerStatsConfiguration.PlayerStats> valueFunction = switch (normalizedCategory) {
            case "kills" -> stats -> stats.kills;
            case "deaths" -> stats -> stats.deaths;
            case "killstreak" -> stats -> stats.killstreak;
            default -> throw new IllegalStateException("Unsupported leaderboard: " + normalizedCategory);
        };

        new Message(
                MessagePrefix.INFO,
                this.formatLeaderboard(title, valueFunction)
        ).sendTo(sender);
    }

    @Suggestions("topCategories")
    public @NotNull Set<String> suggestTopCategories(
            @NotNull final CommandContext<Source> context,
            @NotNull final CommandInput input
    ) {
        return Set.of("kills", "deaths", "killstreak");
    }

    private String formatLeaderboard(
            String title,
            ToIntFunction<PlayerStatsConfiguration.PlayerStats> valueFunction
    ) {
        List<Map.Entry<UUID, PlayerStatsConfiguration.PlayerStats>> entries = new ArrayList<>(
                this.playerStatsService.getAllStats().entrySet()
        );
        entries.sort(
                Comparator.<Map.Entry<UUID, PlayerStatsConfiguration.PlayerStats>>comparingInt(
                                entry -> valueFunction.applyAsInt(entry.getValue())
                        )
                        .reversed()
                        .thenComparing(entry -> this.playerName(entry.getKey()), String.CASE_INSENSITIVE_ORDER)
        );

        StringBuilder leaderboard = new StringBuilder("&f&l").append(title).append("&r");
        int displayedEntries = Math.min(MAX_ENTRIES, entries.size());
        if (displayedEntries == 0) {
            return leaderboard.append("\n&7No stats recorded yet.").toString();
        }

        for (int index = 0; index < displayedEntries; index++) {
            Map.Entry<UUID, PlayerStatsConfiguration.PlayerStats> entry = entries.get(index);
            leaderboard.append("\n&7")
                    .append(index + 1)
                    .append(". &b")
                    .append(this.playerName(entry.getKey()))
                    .append(" &7- &f")
                    .append(valueFunction.applyAsInt(entry.getValue()));
        }
        return leaderboard.toString();
    }

    private String playerName(UUID playerId) {
        String name = Bukkit.getOfflinePlayer(playerId).getName();
        return name == null ? playerId.toString().substring(0, 8) : name;
    }

    private static boolean isSupportedCategory(String category) {
        return category.equals("kills") || category.equals("deaths") || category.equals("killstreak");
    }
}
