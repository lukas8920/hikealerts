package org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GenericPenalizeDict {
    // requires a higher matching score as this is most likely not a hiking trail
    public static final Set<String> penalizeDict =
            new HashSet<>(Arrays.asList("road", "Road", "Street", "street"));
}
