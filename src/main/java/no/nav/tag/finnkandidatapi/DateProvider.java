package no.nav.tag.finnkandidatapi;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateProvider {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }

}
