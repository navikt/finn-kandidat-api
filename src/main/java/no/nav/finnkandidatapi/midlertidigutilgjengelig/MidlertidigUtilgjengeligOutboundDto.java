package no.nav.finnkandidatapi.midlertidigutilgjengelig;

public class MidlertidigUtilgjengeligOutboundDto {
    public MidlertidigUtilgjengelig getMidlertidigUtilgjengelig() {
        return payload;
    }

    private final MidlertidigUtilgjengelig payload;

    public MidlertidigUtilgjengeligOutboundDto(MidlertidigUtilgjengelig payload) {
        this.payload = payload;
    }
}
