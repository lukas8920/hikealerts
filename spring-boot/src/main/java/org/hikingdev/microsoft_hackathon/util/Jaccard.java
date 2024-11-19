package org.hikingdev.microsoft_hackathon.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Jaccard {
    private final Set<String> lowerWeightDict;
    private final Set<String> penalizeDict;

    public Jaccard(Set<String> lowerWeightDict, Set<String> penalizeDict){
        this.lowerWeightDict = lowerWeightDict;
        this.penalizeDict = penalizeDict;
    }

    // Function to calculate the Jaccard Similarity between two sets of words
    public double calculateJaccardSimilarity(String[] words1, String[] words2) {
        if (words2.length < 2 && words1.length < 2) {
            return 0;
        }

        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        double adjustedIntersectionSize = 0;
        double adjustedUnionSize = 0;

        // Calculate adjusted weights for intersection
        for (String word : intersection) {
            if (lowerWeightDict.contains(word)) {
                adjustedIntersectionSize += 0.1;  // Lower weight for dictionary words
            } else {
                adjustedIntersectionSize += 1;
            }
        }

        // Calculate adjusted weights for union
        for (String word : union) {
            if (lowerWeightDict.contains(word)) {
                adjustedUnionSize += 0.1;  // Lower weight for dictionary words
            } else {
                adjustedUnionSize += 1;
            }
        }

        // Check if penalizeDictonary words appear only in one of the sets
        boolean exclusivePenalizeWordFound = false;
        for (String boostWord : penalizeDict) {
            if (set1.contains(boostWord) ^ set2.contains(boostWord)) {
                exclusivePenalizeWordFound = true;
                break;
            }
        }

        double jaccardSimilarity = adjustedUnionSize == 0 ? 0 : adjustedIntersectionSize / adjustedUnionSize;

        // Apply a penalty if an exclusive boost word was found
        return exclusivePenalizeWordFound ? jaccardSimilarity * 0.7 : jaccardSimilarity;
    }
}
