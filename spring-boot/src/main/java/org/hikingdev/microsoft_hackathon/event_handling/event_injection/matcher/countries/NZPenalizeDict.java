package org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.countries;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NZPenalizeDict {
    // requires a higher matching score as this is most likely not a hiking trail
    // helpful if search string contains word but target string does not
    public static final Set<String> penalizeDict =
            new HashSet<>(Arrays.asList("road", "Road", "Street", "street", "Cycle", "cycle"));
}
