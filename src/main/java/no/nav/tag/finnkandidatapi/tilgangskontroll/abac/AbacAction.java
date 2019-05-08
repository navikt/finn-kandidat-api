package no.nav.tag.finnkandidatapi.tilgangskontroll.abac;

public enum AbacAction {

    READ("read"),
    UPDATE("update"),
    PING("ping");

    private String abacKode;

    AbacAction(String abacKode) {
        this.abacKode = abacKode;
    }

    public String getAbacKode() {
        return this.abacKode;
    }
}
