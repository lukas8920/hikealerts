package org.devbros.microsoft_hackathon.event_injection.matcher;

import org.devbros.microsoft_hackathon.event_injection.entities.Trail;
import org.springframework.stereotype.Component;

@Component
public class NameMatcher<T> {
    private T t;

    public void match(String searchName, T t) {

    }

    public T getTopMatchingEntity(){
        return this.t;
    }
}
