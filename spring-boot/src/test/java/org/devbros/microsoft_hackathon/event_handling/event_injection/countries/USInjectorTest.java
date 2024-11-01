package org.devbros.microsoft_hackathon.event_handling.event_injection.countries;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.RawEvent;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Region;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class USInjectorTest extends BaseInjectorTest {
    private static final String country = "US";

    @BeforeEach
    public void setup(){
        super.setup();
        this.injector = new USInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
        super.country = country;
    }

    @Override
    protected void mockTestThatMatchTrailsWorksForTrailname(OpenAiEvent openAiEvent, RawEvent rawEvent, Trail trail){
        when(iRawEventRepository.findRawEvent("1", country)).thenReturn(rawEvent);
        when(iTrailRepository.searchTrailByNameUnitCodeAndCountry(eq(openAiEvent.getTrailName()), eq("abc"), eq(country), any())).thenReturn(trail);
    }

    @Override
    protected void mockTestThatMatchTrailsWorksForTrailFoundViaRegion(RawEvent rawEvent, Region region, Trail trail) {
        when(iRawEventRepository.findRawEvent("1", country)).thenReturn(rawEvent);
        when(iRegionRepository.findUniqueRegionName(eq("region"), eq(country))).thenReturn(List.of(region));
        when(iTrailRepository.findTrailsByNameCodeAndCountry(any(), eq(country), any())).thenReturn(List.of(trail));
    }

    @Override
    protected void mockTestThatMatchTrailsQuitsForEmptyEvents(OpenAiEvent openAiEvent, RawEvent rawEvent){
        when(iRawEventRepository.findRawEvent("1", country)).thenReturn(rawEvent);
        when(iTrailRepository.searchTrailByNameUnitCodeAndCountry(eq(openAiEvent.getTrailName()) , eq("abc"), eq(country), any())).thenReturn(null);
    }

    @Override
    protected void mockTestThatDisplayMidCoordinateWorks(Region region, Trail trail) {
        when(iRegionRepository.findUniqueRegionName(eq("region"), eq(country))).thenReturn(List.of(region));
        when(iTrailRepository.findTrailsByNameCodeAndCountry(any(), eq(country), any())).thenReturn(List.of(trail));
    }
}
