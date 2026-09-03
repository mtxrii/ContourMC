package com.mtxrii.contourmc.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatService {
    private final Map<UUID, Long> combatMap = new ConcurrentHashMap<>();
    private static final long COMBAT_DURATION_MS = 15000; // 15 seconds

    public void tag(UUID playerId) {
        combatMap.put(playerId, System.currentTimeMillis());
    }

    public void unTag(UUID playerId) {
        combatMap.remove(playerId);
    }

    public boolean isInCombat(UUID playerId) {
        if (!this.combatMap.containsKey(playerId)) {
            return false;
        }
        if (System.currentTimeMillis() - this.combatMap.get(playerId) > COMBAT_DURATION_MS) {
            this.combatMap.remove(playerId);
            return false;
        }
        return true;
    }
    
    public long getRemainingCombatTime(UUID playerId) {
        if (!this.combatMap.containsKey(playerId)) {
            return 0;
        }
        long remaining = (this.combatMap.get(playerId) + COMBAT_DURATION_MS) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
}
