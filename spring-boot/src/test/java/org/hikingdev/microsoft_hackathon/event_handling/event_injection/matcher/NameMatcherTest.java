package org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 *
 */
public class NameMatcherTest {
    private static final double US_MATCHER_THRESHOLD = 0.15;
    private static final double US_LEVENSHTEIN_WEIGHT = 0.62;

    @Test
    public void testUSStringMatching(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);
        String searchString = "New Hestigae Trails";
        String targetString1 = "New Hampshire Hestige Trail";
        String targetString2 = "Old Hestigae Trail";
        String targetString3 = "Neww Hestigea Trail";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);
        Trail trail2 = new Trail();
        trail2.setTrailname(targetString2);
        Trail trail3 = new Trail();
        trail3.setTrailname(targetString3);

        nameMatcher.match(searchString, trail1);
        nameMatcher.match(searchString, trail2);
        nameMatcher.match(searchString, trail3);

        assertThat(nameMatcher.getT(), is(trail1));
    }

    @Test
    public void testUSNonMatching(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);

        String searchString = "New Hestigae Trails";
        String targetString1 = "New Agis Camp";
        String targetString2 = "Carantee Trails";
        String targetString3 = "Kapache Trail";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);
        Trail trail2 = new Trail();
        trail2.setTrailname(targetString2);
        Trail trail3 = new Trail();
        trail3.setTrailname(targetString3);

        nameMatcher.match(searchString, trail1);
        nameMatcher.match(searchString, trail2);
        nameMatcher.match(searchString, trail3);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testUsNonMatchingRoad(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);

        String searchString = "Paradise Hope Road";
        String targetString1 = "Paradise Hope Peak";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testUsNonMatchingRoad2(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);

        String searchString = "Loop Road";
        String targetString1 = "Loop Circuit";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testUsNonMatchingTrail(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);

        String searchString = "River Trail";
        String targetString1 = "Trailer Village";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testUsMatchingTrail(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);

        String searchString = "Boquillas Canyon Trail";
        String targetString1 = "Boquillas Canyon";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), is(trail1));
    }
}
