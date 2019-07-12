package no.nav.tag.finnkandidatapi.tilbakemelding;

import lombok.Value;

@Value
public class Tilbakemelding {
    private Behov behov;
    private String tilbakemelding;
}
