package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.RawEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Region;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class NZInjectorTest extends RegionInjectorTest {
    private static final String country = "NZ";

    @BeforeEach
    public void setup(){
        super.setup();
        this.injector = new NZInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
        super.country = country;
    }

    @Override
    protected void mockTestThatMatchTrailsWorksForTrail(OpenAiEvent openAiEvent, RawEvent rawEvent, Trail trail) {
        when(iRawEventRepository.findRawEvent("1", country)).thenReturn(rawEvent);
        when(iTrailRepository.searchTrailByNameAndCountry(eq(openAiEvent.getTrailName()), eq(country), any())).thenReturn(trail);
    }

    @Override
    protected void setPublisherId(RawEvent rawEvent) {
        rawEvent.setPublisherId(2L);
    }

    @Override
    protected void mockTestThatMatchTrailsWorksForTrailFoundViaRegion(RawEvent rawEvent, Region region, Trail trail) {
        when(iRawEventRepository.findRawEvent("1", country)).thenReturn(rawEvent);
        when(iRegionRepository.findUniqueRegionName(eq("region"), eq(country))).thenReturn(List.of(region));
        when(iTrailRepository.findTrailsInRegion(any(), eq(country))).thenReturn(List.of(trail));
    }

    @Override
    protected void mockTestThatMatchTrailsQuitsForEmptyEvents(OpenAiEvent openAiEvent, RawEvent rawEvent) {
        when(iRawEventRepository.findRawEvent("1", country)).thenReturn(rawEvent);
        when(iTrailRepository.searchTrailByNameAndCountry(eq(openAiEvent.getTrailName()) , eq(country), any())).thenReturn(null);
    }

    @Override
    protected void mockTestThatDisplayMidCoordinateWorks(Region region, Trail trail) {
        when(iRegionRepository.findUniqueRegionName(eq("region"), eq(country))).thenReturn(List.of(region));
        when(iTrailRepository.findTrailsInRegion(any(), eq(country))).thenReturn(List.of(trail));
    }
}
