package no.nav.finnkandidatapi.tilbakemelding;

import lombok.Value;

@Value
public class Tilbakemelding {
    private Behov behov;
    private String tilbakemelding;
}
