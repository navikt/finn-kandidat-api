package no.nav.finnkandidatapi.logging;

import lombok.Value;
import net.minidev.json.JSONObject;

@Value
public class LoggEvent {
    private String name;
    private JSONObject tags;
    private JSONObject fields;
}
