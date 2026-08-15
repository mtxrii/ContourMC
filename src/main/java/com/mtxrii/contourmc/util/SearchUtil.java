package com.mtxrii.contourmc.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class SearchUtil {

    /**
     * Finds the best matching player name.
     * <p>
     * Priority:
     *   1. Exact match
     *   2. Case-insensitive exact match
     *   3. Prefix match
     *   4. Contains match
     *   5. Fuzzy match (Levenshtein, max distance 2)
     *   6. null if nothing is close enough
     */
    public static String findClosestPlayerNameMatch(String input, String[] candidates) {
        if (input == null || candidates == null || candidates.length == 0) {
            return null;
        }

        input = input.trim();
        if (input.isEmpty()) {
            return null;
        }

        // 1. Exact (case-sensitive)
        for (String candidate : candidates) {
            if (candidate != null && candidate.equals(input)) {
                return candidate;
            }
        }

        String lowerInput = input.toLowerCase();

        // 2. Exact (case-insensitive)
        for (String candidate : candidates) {
            if (candidate != null && candidate.equalsIgnoreCase(input)) {
                return candidate;
            }
        }

        String bestMatch = null;
        int bestScore = Integer.MIN_VALUE;

        for (String candidate : candidates) {
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }

            String lowerCandidate = candidate.toLowerCase();
            int score = Integer.MIN_VALUE;

            // 3. Prefix match
            if (lowerCandidate.startsWith(lowerInput)) {
                score = 1000 - (candidate.length() - input.length());
            }
            // 4. Contains match
            else if (lowerCandidate.contains(lowerInput)) {
                score = 500 - lowerCandidate.indexOf(lowerInput);
            }
            // 5. Fuzzy match
            else {
                int lengthDifference = Math.abs(lowerInput.length() - lowerCandidate.length());

                // Ignore names that differ too much in length
                if (lengthDifference > 2) {
                    continue;
                }

                int distance = levenshteinDistance(lowerInput, lowerCandidate);

                // Ignore names that are too different
                if (distance > 2) {
                    continue;
                }

                score = 100 - distance;
            }

            if (score > bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }

        return bestMatch;
    }

    private static int levenshteinDistance(String a, String b) {
        int[] costs = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) {
            costs[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int northwest = i - 1;

            for (int j = 1; j <= b.length(); j++) {
                int north = costs[j];

                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;

                costs[j] = Math.min(
                        Math.min(costs[j] + 1, costs[j - 1] + 1),
                        northwest + cost
                );

                northwest = north;
            }
        }

        return costs[b.length()];
    }
}
