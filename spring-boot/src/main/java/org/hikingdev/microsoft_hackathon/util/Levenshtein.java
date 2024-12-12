package org.hikingdev.microsoft_hackathon.util;

import java.util.Set;

public class Levenshtein {
    private final Set<String> lowerWeightDict;
    private final Set<String> penalizeDict;

    public Levenshtein(Set<String> lowerWeightDict, Set<String> penalizeDict){
        this.lowerWeightDict = lowerWeightDict;
        this.penalizeDict = penalizeDict;
    }

    public double calculateLevenshteinSimilarity(String word1, String word2) {
        int distance = calculateCustomLevenshtein(word1, word2);

        // Check if any boost word appears only in one of the strings
        boolean penalizeWordFound = false;
        for (String boostWord : penalizeDict) {
            if (word1.contains(boostWord) ^ word2.contains(boostWord)) {
                penalizeWordFound = true;
                break;
            }
        }

        // Normalize to be a similarity score between 0 and 1
        int maxLength = Math.max(word1.length(), word2.length());
        double levenshteinSimilarity = maxLength == 0 ? 1.0 : Math.max(0, 1.0 - ((double) distance / maxLength));
        return penalizeWordFound ? levenshteinSimilarity * 0.7 : levenshteinSimilarity;
    }

    // used for h2 test purposes
    public static int levenshtein(String s, String t, int max) {
        int sLen = s.length();
        int tLen = t.length();
        int[][] dist = new int[sLen + 1][tLen + 1];

        // Initialize distance matrix
        for (int i = 0; i <= sLen; i++) {
            dist[i][0] = i;
        }
        for (int j = 0; j <= tLen; j++) {
            dist[0][j] = j;
        }

        for (int i = 1; i <= sLen; i++) {
            for (int j = 1; j <= tLen; j++) {
                int cost = s.charAt(i - 1) == t.charAt(j - 1) ? 0 : 1;
                dist[i][j] = Math.min(Math.min(
                                dist[i - 1][j] + 1,          // Deletion
                                dist[i][j - 1] + 1),         // Insertion
                        dist[i - 1][j - 1] + cost);  // Substitution
            }
        }

        int result = dist[sLen][tLen];
        return result <= max ? result : -1; // Return null equivalent as -1 if greater than max
    }

    private int calculateCustomLevenshtein(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        // Initialize DP table
        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;

        // Fill DP table with custom costs
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int substitutionCost = (word1.charAt(i - 1) == word2.charAt(j - 1)) ? 0 : getSubstitutionCost(word1.charAt(i - 1), word2.charAt(j - 1));

                dp[i][j] = Math.min(dp[i - 1][j] + getDeletionCost(word1.charAt(i - 1)),
                        Math.min(dp[i][j - 1] + getInsertionCost(word2.charAt(j - 1)),
                                dp[i - 1][j - 1] + substitutionCost));
            }
        }

        return dp[len1][len2];
    }

    private int getSubstitutionCost(char c1, char c2) {
        String s1 = String.valueOf(c1);
        String s2 = String.valueOf(c2);
        if (this.lowerWeightDict.contains(s1) || this.lowerWeightDict.contains(s2)) {
            return 1;  // Lower substitution cost for dictionary words
        }
        return 2;  // Standard cost for non-dictionary words
    }

    private int getInsertionCost(char c) {
        return this.lowerWeightDict.contains(String.valueOf(c)) ? 1 : 2;
    }

    private int getDeletionCost(char c) {
        return this.lowerWeightDict.contains(String.valueOf(c)) ? 1 : 2;
    }
}
