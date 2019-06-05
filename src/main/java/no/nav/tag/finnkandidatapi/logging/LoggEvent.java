package no.nav.tag.finnkandidatapi.logging;

import lombok.Data;
import net.minidev.json.JSONObject;

@Data
public class LoggEvent {
    private String eventnavn;
    private JSONObject felter;
}
