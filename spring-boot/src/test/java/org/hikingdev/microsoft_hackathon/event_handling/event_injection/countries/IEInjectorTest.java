package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.RawEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class IEInjectorTest extends BaseInjectorTest {
    private static final String country = "IE";

    @BeforeEach
    public void setup(){
        super.setup();
        this.injector = new IEInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
        super.country = country;
    }

    @Override
    protected void mockTestThatMatchTrailsWorksForTrailname(OpenAiEvent openAiEvent, RawEvent rawEvent, Trail trail) {
        rawEvent.setDescription("dummy. test description");
        when(iRawEventRepository.findRawEvent("1", country)).thenReturn(rawEvent);
        when(iTrailRepository.searchTrailByNameAndCountry(eq(openAiEvent.getTrailName()), eq(country), any())).thenReturn(trail);
    }

    @Override
    protected void mockTestThatMatchTrailsQuitsForEmptyEvents(OpenAiEvent openAiEvent, RawEvent rawEvent) {
        rawEvent.setDescription("dummy. test description");
        when(iRawEventRepository.findRawEvent("1", country)).thenReturn(rawEvent);
        when(iTrailRepository.searchTrailByNameAndCountry(eq(openAiEvent.getTrailName()) , eq(country), any())).thenReturn(null);
    }
}
