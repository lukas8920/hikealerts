package org.devbros.microsoft_hackathon.event_injection.countries;

import org.devbros.microsoft_hackathon.event_injection.repository.events.IEventRepository;
import org.devbros.microsoft_hackathon.event_injection.repository.raw_events.IRawEventRepository;
import org.devbros.microsoft_hackathon.event_injection.repository.regions.IRegionRepository;
import org.devbros.microsoft_hackathon.event_injection.repository.trails.ITrailRepository;

public class USInjector extends BaseCountryInjector {
    public USInjector(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository, ITrailRepository iTrailRepository, IRegionRepository iRegionRepository) {
        super(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
    }
}
