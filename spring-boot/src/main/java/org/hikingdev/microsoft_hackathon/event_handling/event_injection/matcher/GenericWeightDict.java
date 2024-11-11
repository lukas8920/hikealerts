package org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GenericWeightDict {
    // weak matching words as almost every trail contains those words
    public static final Set<String> lowerWeightDict =
            new HashSet<>(Arrays.asList("trail", "Trail", "way", "Way"));
}
