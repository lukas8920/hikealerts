package org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.countries;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NZWeightDict {
    // weak matching words as almost every trail contains those words
    // helpful if target string contains word, but is a no match
    public static final Set<String> lowerWeightDict =
            new HashSet<>(Arrays.asList("track", "Track", "Walkway", "walkway", "Walk", "walk", "Route", "route", "Hut", "hut", "Waterfall", "waterfall", "Forest", "forest", "Summit", "summit"));
}
