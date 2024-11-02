package org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher;

import lombok.Getter;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MatchProvider;
import org.hikingdev.microsoft_hackathon.util.Jaccard;
import org.hikingdev.microsoft_hackathon.util.Levenshtein;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Getter
public class NameMatcher<T extends MatchProvider> {
    private static final Logger logger = LoggerFactory.getLogger(NameMatcher.class.getName());

    private Levenshtein levenshtein;
    private Jaccard jaccard;

    private final double matchingThreshold;
    private final double levenshteinWeight;

    private T t;
    private double matchingScore = 1;

    public NameMatcher(double matchingThreshold, double levenshteinWeight){
        this.matchingThreshold = matchingThreshold;
        this.matchingScore = matchingThreshold;
        this.levenshteinWeight = levenshteinWeight;

        Set<String> emptySet = new HashSet<>();
        this.levenshtein = new Levenshtein(emptySet, emptySet);
        this.jaccard = new Jaccard(emptySet, emptySet);
    }

    public NameMatcher(Set<String> lowerWeightDict, Set<String> penalizeDict, double matchingThreshold, double levenshteinWeight){
        this(matchingThreshold, levenshteinWeight);

        this.levenshtein = new Levenshtein(lowerWeightDict, penalizeDict);
        this.jaccard = new Jaccard(lowerWeightDict, penalizeDict);
    }

    public void resetNameMatcher(){
        this.t = null;
        this.matchingScore = this.matchingThreshold;
    }

    public void match(String searchName, T t) {
        if (searchName == null || searchName.length() < 1){
            return;
        }

        String[] candidateStrings = t.getCandidateStrings();
        String[] words1 = searchName.split("\\s+");

        for (String str: candidateStrings){
            logger.debug("Search string: " + str);
            logger.debug("Candidate string: " + str);
            if (str == null || str.length() < 1){
                break;
            }

            // Split the strings into words
            String[] words2 = str.split("\\s+");

            // 1. Calculate Jaccard Similarity for the overall word match
            double jaccardSimilarity = jaccard.calculateJaccardSimilarity(words1, words2);
            logger.debug("Jaccard: " + jaccardSimilarity);

            // 2. Calculate Levenshtein similarity for each word pair (where words don't exactly match)
            double totalLevenshteinSimilarity = 0.0;
            int levenshteinComparisons = 0;

            for (String word1 : words1) {
                for (String word2 : words2) {
                    // Only compare if the words are different
                    if (!word1.equals(word2)) {
                        totalLevenshteinSimilarity += levenshtein.calculateLevenshteinSimilarity(word1, word2);
                        levenshteinComparisons++;
                    }
                }
            }

            // Average Levenshtein similarity (if any comparisons were made)
            double averageLevenshteinSimilarity = levenshteinComparisons > 0 ?
                    totalLevenshteinSimilarity / levenshteinComparisons : 1.0; // 1.0 if exact match

            // 3. Combine Jaccard and Levenshtein, giving 50% weight to each
            double jaccard_weight = 1 - levenshteinWeight;
            double combinedScore = jaccard_weight * jaccardSimilarity + levenshteinWeight * averageLevenshteinSimilarity;

            logger.debug("Score: " + combinedScore);
            // 4. Check whether optimum pick
            if (combinedScore > matchingScore){
                this.matchingScore = combinedScore;
                this.t = t;
                return;
            }
        }
    }
}
