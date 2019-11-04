package no.nav.tag.finnkandidatapi.unleash.enhet;

import lombok.RequiredArgsConstructor;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.strategy.Strategy;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class ByEnhetStrategy implements Strategy {

    static final String PARAM = "valgtEnhet";
    private final AxsysService axsysService;

    @Override
    public String getName() {
        return "byEnhet";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return unleashContext.getUserId()
                .flatMap(currentUserId -> Optional.ofNullable(parameters.get(PARAM))
                        .map(enheterString -> Set.of(enheterString.split(",\\s?")))
                        .map(enabledeEnheter -> !Collections.disjoint(enabledeEnheter, brukersEnheter(currentUserId))))
                .orElse(false);
    }

    private List<String> brukersEnheter(String currentUserId) {
        return axsysService.hentEnheterVeilederHarTilgangTil(currentUserId).stream()
                .map(enhet -> enhet.getVerdi()).collect(toList());
    }

}
