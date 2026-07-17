package com.mtxrii.contourmc;

public enum Rank {
    PLAYER,
    MEDIATOR,
    STAFF;

    public static Rank get(String rankName) {
        try {
            return Rank.valueOf(rankName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
