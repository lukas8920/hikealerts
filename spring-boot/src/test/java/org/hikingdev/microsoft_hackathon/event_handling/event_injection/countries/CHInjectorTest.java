package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.RawEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CHInjectorTest extends BaseInjectorTest {
    private static final String country = "CH";

    @BeforeEach
    public void setup(){
        super.setup();
        this.injector = new CHInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
        super.country = country;
    }

    @Override
    protected void mockTestThatMatchTrailsWorksForTrail(OpenAiEvent openAiEvent, RawEvent rawEvent, Trail trail) {
        rawEvent.setDescription("dummy. test description");
        when(iRawEventRepository.findRawEvent("1", country)).thenReturn(rawEvent);
        when(iTrailRepository.findTrailByIdAndCountry(eq("1") , eq(country))).thenReturn(trail);
    }

    @Override
    protected void mockTestThatMatchTrailsQuitsForEmptyEvents(OpenAiEvent openAiEvent, RawEvent rawEvent) {
        rawEvent.setDescription("dummy. test description");
        when(iRawEventRepository.findRawEvent("1", country)).thenReturn(rawEvent);
        when(iTrailRepository.findTrailByIdAndCountry(any() , eq(country))).thenReturn(null);
    }
}
