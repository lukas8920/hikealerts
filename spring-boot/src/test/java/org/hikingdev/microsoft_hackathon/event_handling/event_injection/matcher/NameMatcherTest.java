package org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.countries.NZPenalizeDict;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.countries.NZWeightDict;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 *
 */
public class NameMatcherTest {
    private static final double US_MATCHER_THRESHOLD = 0.175;
    private static final double US_LEVENSHTEIN_WEIGHT = 0.62;

    private static final double NZ_MATCHER_THRESHOLD = 0.15;
    private static final double NZ_LEVENSHTEIN_WEIGHT = 0.62;

    @Test
    public void testUSStringMatching(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);
        String searchString = "New Hestigae Trail";
        String targetString1 = "New Hampshire Hestigae Trail";
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
    public void testIEStringMatching(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);
        String searchString = "Dingle Way";
        String targetString1 = "Dingle Way";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

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
    public void testUsNonMatchingRoad3(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);

        String searchString = "Mauna Loa Road";
        String targetString1 = "Mauna Loa Trail";

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

    @Test
    public void testUsMatchingTrail2(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);

        String searchString = "Wupatki Pueblo Trail";
        String targetString1 = "Wupatki Pueblo";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), is(trail1));
    }

    @Test
    public void testUsMatchingTrail3(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);

        String searchString = "Dungeness Beach";
        String targetString1 = "Dungeness Beach Crossing";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), is(trail1));
    }

    @Test
    public void testUsMatchingTrail4(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);

        String searchString = "Juniper-Hercules Leg";
        String targetString1 = "Hercules Leg Cave";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNoneTargetString(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, US_MATCHER_THRESHOLD, US_LEVENSHTEIN_WEIGHT);

        String searchString = "Glacier Point Road";
        String targetString1 = " ";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzNonMatchingTrail(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Smith Creek Track";
        String targetString1 = "Boyd Creek Track";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzNonMatchingTrail2(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Kiwi Burn Track";
        String targetString1 = "Craig Burn Track";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzNonMatchingTrail3(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Goldie Bush Walkway";
        String targetString1 = "Grahams Bush Walkway";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzNonMatchingTrail4(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Hobbs Beach Walkway";
        String targetString1 = "Allans Walkway";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzNonMatchingTrail5(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Te Waiti Track";
        String targetString1 = "Te Au Track";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzMatchingTrail(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Isthmus Track";
        String targetString1 = "Isthmus Peak Track";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), is(trail1));
    }

    @Test
    public void testNzMatchingTrail2(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Bain Bay Walk";
        String targetString1 = "Karikari Bay Walk";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzNonMatchingTrail6(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, NZPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "St James Cycle Trail";
        String targetString1 = "St James Walkway";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzNonMatchingTrail7(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, NZPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Old Woman Hut";
        String targetString1 = "Old Man Hut Circuit";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzNonMatchingTrail8(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, NZPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Old Woman Hut";
        String targetString1 = "Old Man Hut Circuit";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzNonMatchingTrail9(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, NZPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Herekino Forest Track";
        String targetString1 = "Forest Walk";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzNonMatchingTrail10(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, NZPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Swampy Summit Track";
        String targetString1 = "Summit Track";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), nullValue());
    }

    @Test
    public void testNzMatchingTrail3(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>(NZWeightDict.lowerWeightDict, NZPenalizeDict.penalizeDict, NZ_MATCHER_THRESHOLD, NZ_LEVENSHTEIN_WEIGHT);

        String searchString = "Asbestos Cottage Track";
        String targetString1 = "Asbestos Cottage tracks";

        Trail trail1 = new Trail();
        trail1.setTrailname(targetString1);

        nameMatcher.match(searchString, trail1);

        assertThat(nameMatcher.getT(), is(trail1));
    }
}
