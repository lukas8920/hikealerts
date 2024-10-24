package org.devbros.microsoft_hackathon.event_handling.event_injection.matcher;

import lombok.Getter;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.MatchProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Component
public class NameMatcher<T extends MatchProvider> {
    private static final Logger logger = LoggerFactory.getLogger(NameMatcher.class.getName());
    private static final double MATCHING_THRESHOLD = 0.19;

    private T t;
    private double matchingScore = MATCHING_THRESHOLD;

    public void resetNameMatcher(){
        this.t = null;
        this.matchingScore = MATCHING_THRESHOLD;
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
            double jaccardSimilarity = calculateJaccardSimilarity(words1, words2);
            logger.debug("Jaccard: " + jaccardSimilarity);

            // 2. Calculate Levenshtein similarity for each word pair (where words don't exactly match)
            double totalLevenshteinSimilarity = 0.0;
            int levenshteinComparisons = 0;

            for (String word1 : words1) {
                for (String word2 : words2) {
                    // Only compare if the words are different
                    if (!word1.equals(word2)) {
                        totalLevenshteinSimilarity += calculateLevenshteinSimilarity(word1, word2);
                        levenshteinComparisons++;
                    }
                }
            }

            // Average Levenshtein similarity (if any comparisons were made)
            double averageLevenshteinSimilarity = levenshteinComparisons > 0 ?
                    totalLevenshteinSimilarity / levenshteinComparisons : 1.0; // 1.0 if exact match

            // 3. Combine Jaccard and Levenshtein, giving 50% weight to each
            double combinedScore = 0.38 * jaccardSimilarity + 0.62 * averageLevenshteinSimilarity;

            logger.debug("Score: " + combinedScore);
            // 4. Check whether optimum pick
            if (combinedScore > matchingScore){
                this.matchingScore = combinedScore;
                this.t = t;
                return;
            }
        }
    }

    // Function to calculate the Jaccard Similarity between two sets of words
    private double calculateJaccardSimilarity(String[] words1, String[] words2) {
        if (words2.length < 3 && words1.length < 3){
            return 0;
        }

        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    // Function to calculate the Levenshtein Distance between two words and normalize it
    private double calculateLevenshteinSimilarity(String word1, String word2) {
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int distance = levenshtein.apply(word1, word2);

        // Normalize the distance to be a similarity score between 0 and 1
        int maxLength = Math.max(word1.length(), word2.length());
        return 1.0 - ((double) distance / maxLength);  // Similarity (1 is perfect match)
    }
}
