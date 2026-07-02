package com.mtxrii.contourmc.message;

public enum MessagePrefix {
    INFO, KIT, SPAWN, ENV, GAME;

    public String getFormatted() {
        int messagePrefixLength = this.name().length();
        String padding = " ".repeat(prefixLength() - messagePrefixLength);
        return this.name() + padding + " &9&l| ";
    }

    private static int prefixLength() {
        int longestMessagePrefixLength = 0;
        for (MessagePrefix prefix : values()) {
            longestMessagePrefixLength = Math.max(longestMessagePrefixLength, prefix.name().length());
        }
        return longestMessagePrefixLength;
    }
}
