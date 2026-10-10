package com.mtxrii.contourmc.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mtxrii.contourmc.config.PlayerStatsConfiguration;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.RenderType;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@com.sxtanna.platform.archetype.Component
@Singleton
public final class ScoreboardService {
    private static final String OBJECTIVE_NAME = "contour_sidebar";
    private static final Component OBJECTIVE_TITLE = Component.text("✦ CONTOURMC ✦");

    private final PlayerStatsService playerStatsService;
    private final PlayerRegistryService playerRegistryService;
    private final CombatService combatService;
    private final Map<UUID, Scoreboard> playerScoreboards = new ConcurrentHashMap<>();

    @Inject
    public ScoreboardService(
            PlayerStatsService playerStatsService,
            PlayerRegistryService playerRegistryService,
            CombatService combatService
    ) {
        this.playerStatsService = playerStatsService;
        this.playerRegistryService = playerRegistryService;
        this.combatService = combatService;
    }

    /** Refreshes the sidebar for every online player and removes stale boards. */
    public void updateAll() {
        Set<UUID> onlinePlayerIds = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            onlinePlayerIds.add(player.getUniqueId());
            this.updatePlayer(player);
        }

        this.playerScoreboards.keySet().removeIf(playerId -> {
            if (onlinePlayerIds.contains(playerId)) {
                return false;
            }
            Scoreboard scoreboard = this.playerScoreboards.get(playerId);
            this.unregister(scoreboard);
            return true;
        });
    }

    /** Removes a player's sidebar when they leave the server. */
    public void removePlayer(@NotNull UUID playerId) {
        Scoreboard scoreboard = this.playerScoreboards.remove(playerId);
        this.unregister(scoreboard);
    }

    /** Removes all managed scoreboards during plugin shutdown. */
    public void clear() {
        for (Scoreboard scoreboard : this.playerScoreboards.values()) {
            this.unregister(scoreboard);
        }
        this.playerScoreboards.clear();
    }

    private void updatePlayer(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        Scoreboard scoreboard = this.playerScoreboards.computeIfAbsent(
                playerId,
                ignored -> Bukkit.getScoreboardManager().getNewScoreboard()
        );
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = scoreboard.registerNewObjective(
                    OBJECTIVE_NAME,
                    Criteria.DUMMY,
                    OBJECTIVE_TITLE,
                    RenderType.INTEGER
            );
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.numberFormat(NumberFormat.blank());
        }

        if (player.getScoreboard() != scoreboard) {
            player.setScoreboard(scoreboard);
        }

        for (String entry : new ArrayList<>(scoreboard.getEntries())) {
            scoreboard.resetScores(entry);
        }

        PlayerStatsConfiguration.PlayerStats stats = this.playerStatsService.getStats(playerId);
        List<String> lines = this.sidebarLines(player, stats);
        for (int index = 0; index < lines.size(); index++) {
            scoreboard.getObjective(OBJECTIVE_NAME)
                    .getScore(lines.get(index))
                    .setScore(lines.size() - index);
        }
    }

    private List<String> sidebarLines(
            Player player,
            PlayerStatsConfiguration.PlayerStats stats
    ) {
        List<String> lines = new ArrayList<>();
        lines.add("§0"); // @TODO: Abstract the manual section sign insertions
        lines.add("§fKills: §b" + stats.kills);
        lines.add("§fDeaths: §b" + stats.deaths);
        lines.add("§fKillstreak: §b" + stats.killstreak);
        lines.add("§8");
        lines.add("§fKit: §b" + this.playerRegistryService.getCurrentKit(player.getUniqueId()));
        lines.add("§fCombat: §b" + this.combatTime(player.getUniqueId()));
        lines.add("§fPlayers: §b" + Bukkit.getOnlinePlayers().size());
        lines.add("§7");
        lines.add("§7play.contourmc.com");
        return lines;
    }

    private String combatTime(UUID playerId) {
        if (!this.combatService.isInCombat(playerId)) {
            return "Safe";
        }
        long remainingMilliseconds = this.combatService.getRemainingCombatTime(playerId);
        long remainingSeconds = (remainingMilliseconds + 999) / 1000;
        return remainingSeconds + "s";
    }

    private void unregister(Scoreboard scoreboard) {
        if (scoreboard == null) {
            return;
        }
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective != null) {
            objective.unregister();
        }
    }
}
