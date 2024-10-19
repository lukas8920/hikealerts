package org.devbros.microsoft_hackathon.event_handling.event_injection.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MapEvents {
    private long total;
    private List<Event> events;
}
