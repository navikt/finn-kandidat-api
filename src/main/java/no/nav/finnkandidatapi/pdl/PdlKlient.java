package no.nav.finnkandidatapi.pdl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PdlKlient {

    private String url;

    public PdlKlient() {
    }

    public String tilFnr(String aktørId) {
        // TODO
        // historisk: false
        return "";
    }

    public String tilAktørId(String fnr) {
        // TODO
        return "";
    }
}
