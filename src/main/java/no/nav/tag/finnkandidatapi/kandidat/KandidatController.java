package no.nav.tag.finnkandidatapi.kandidat;


import lombok.RequiredArgsConstructor;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.finnkandidatapi.tilgangskontroll.TilgangskontrollService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Protected
@RestController
@RequestMapping("/kandidater")
@RequiredArgsConstructor
public class KandidatController {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final KandidatService kandidatService;
    private final TilgangskontrollService tilgangskontroll;

    @GetMapping("/{fnr}")
    public ResponseEntity<Kandidat> hentKandidat(@PathVariable("fnr") String fnr) {
        tilgangskontroll.sjekkLesetilgangTilKandidat(fnr);

        Kandidat kandidat = kandidatService.hentNyesteKandidat(fnr).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(kandidat);
    }

    @GetMapping
    public ResponseEntity<List<Kandidat>> hentKandidater() {
        List<Kandidat> kandidater = kandidatService.hentKandidater();
        return ResponseEntity.ok(kandidater);
    }

    @PostMapping
    public ResponseEntity<Kandidat> lagreKandidat(@RequestBody Kandidat kandidat) {
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(kandidat.getFnr());

        Veileder veileder = tilgangskontroll.hentInnloggetVeileder();
        Kandidat lagretKandidat = kandidatService.lagreKandidat(kandidat, veileder).orElseThrow(FinnKandidatException::new);
        applicationEventPublisher.publishEvent(lagretKandidat);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lagretKandidat);
    }

    @GetMapping("/{fnr}/skrivetilgang")
    public ResponseEntity hentSkrivetilgang(@PathVariable("fnr") String fnr) {
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(fnr);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{fnr}")
    public ResponseEntity<String> slettKandidat(@PathVariable("fnr") String fnr) {
        tilgangskontroll.sjekkSkrivetilgangTilKandidat(fnr);

        Integer antallSlettedeRader = kandidatService.slettKandidat(fnr);

        if (antallSlettedeRader == 0) {
            throw new NotFoundException();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/discovery")
    public ResponseEntity<String> discovery() {
        String str = "{\n" +
                "  \"response_types_supported\": [\n" +
                "    \"code token id_token\",\n" +
                "    \"code\",\n" +
                "    \"code id_token\",\n" +
                "    \"id_token\",\n" +
                "    \"code token\",\n" +
                "    \"token\",\n" +
                "    \"token id_token\"\n" +
                "  ],\n" +
                "  \"claims_parameter_supported\": false,\n" +
                "  \"end_session_endpoint\": \"https://isso-q.adeo.no:443/isso/oauth2/connect/endSession\",\n" +
                "  \"version\": \"3.0\",\n" +
                "  \"check_session_iframe\": \"https://isso-q.adeo.no:443/isso/oauth2/connect/checkSession\",\n" +
                "  \"scopes_supported\": [\n" +
                "    \"openid\"\n" +
                "  ],\n" +
                "  \"issuer\": \"https://isso-q.adeo.no:443/isso/oauth2\",\n" +
                "  \"id_token_encryption_enc_values_supported\": [\n" +
                "    \"A128CBC-HS256\",\n" +
                "    \"A256CBC-HS512\"\n" +
                "  ],\n" +
                "  \"acr_values_supported\": [],\n" +
                "  \"authorization_endpoint\": \"https://isso-q.adeo.no:443/isso/oauth2/authorize\",\n" +
                "  \"userinfo_endpoint\": \"https://isso-q.adeo.no:443/isso/oauth2/userinfo\",\n" +
                "  \"claims_supported\": [],\n" +
                "  \"id_token_encryption_alg_values_supported\": [\n" +
                "    \"RSA1_5\"\n" +
                "  ],\n" +
                "  \"jwks_uri\": \"https://isso-q.adeo.no:443/isso/oauth2/connect/jwk_uri\",\n" +
                "  \"subject_types_supported\": [\n" +
                "    \"public\"\n" +
                "  ],\n" +
                "  \"id_token_signing_alg_values_supported\": [\n" +
                "    \"ES384\",\n" +
                "    \"HS256\",\n" +
                "    \"HS512\",\n" +
                "    \"ES256\",\n" +
                "    \"RS256\",\n" +
                "    \"HS384\",\n" +
                "    \"ES512\"\n" +
                "  ],\n" +
                "  \"registration_endpoint\": \"https://isso-q.adeo.no:443/isso/oauth2/connect/register\",\n" +
                "  \"token_endpoint_auth_methods_supported\": [\n" +
                "    \"client_secret_post\",\n" +
                "    \"private_key_jwt\",\n" +
                "    \"client_secret_basic\"\n" +
                "  ],\n" +
                "  \"token_endpoint\": \"https://isso-q.adeo.no:443/isso/oauth2/access_token\"\n" +
                "}";
        return ResponseEntity.ok(str);
    }
}
