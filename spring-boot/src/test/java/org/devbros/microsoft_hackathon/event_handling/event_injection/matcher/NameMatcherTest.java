package org.devbros.microsoft_hackathon.event_handling.event_injection.matcher;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 *
 */
public class NameMatcherTest {
    @Test
    public void testStringMatching(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>();
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
    public void testNonMatching(){
        NameMatcher<Trail> nameMatcher = new NameMatcher<>();

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
}
