package no.nav.tag.finnkandidatapi.kandidat;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static no.nav.tag.finnkandidatapi.kandidat.KandidatMapper.enumSetTilString;

@Slf4j
@Repository
public class KandidatRepository {

    static final String KANDIDAT_TABELL = "kandidat";
    static final String ID = "id";
    static final String FNR = "fnr";
    static final String AKTØR_ID = "aktor_id";
    static final String REGISTRERT_AV = "registrert_av";
    static final String REGISTRERT_AV_BRUKERTYPE = "registrert_av_brukertype";
    static final String REGISTRERINGSTIDSPUNKT = "registreringstidspunkt";
    static final String ARBEIDSTID_BEHOV = "arbeidstid_behov";
    static final String FYSISKE_BEHOV = "fysiske_behov";
    static final String ARBEIDSMILJØ_BEHOV = "arbeidsmiljø_behov";
    static final String GRUNNLEGGENDE_BEHOV = "grunnleggende_behov";
    static final String SLETTET = "slettet";
    static final String OPPRETTET = "opprettet";
    static final String NAV_KONTOR = "nav_kontor";

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;
    private final KandidatMapper kandidatMapper;
    private final HarTilretteleggingsbehovMapper harTilretteleggingsbehovMapper;

    @Autowired
    public KandidatRepository(JdbcTemplate jdbcTemplate, SimpleJdbcInsert simpleJdbcInsert, KandidatMapper kandidatMapper, HarTilretteleggingsbehovMapper harTilretteleggingsbehovMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = simpleJdbcInsert
                .withTableName(KANDIDAT_TABELL)
                .usingGeneratedKeyColumns(ID);
        this.kandidatMapper = kandidatMapper;
        this.harTilretteleggingsbehovMapper = harTilretteleggingsbehovMapper;
    }

    public Optional<Kandidat> hentNyesteKandidat(String aktørId) {
        try {
            Kandidat kandidat = jdbcTemplate.queryForObject(
                    "SELECT * FROM kandidat WHERE (aktor_id = ?) ORDER BY registreringstidspunkt DESC LIMIT 1", new Object[]{aktørId},
                    kandidatMapper
            );
            return Optional.ofNullable(kandidat);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Kandidat> hentKandidat(Integer id) {
        try {
            Kandidat kandidat = jdbcTemplate.queryForObject(
                    "SELECT * FROM kandidat WHERE id = ?", new Object[]{id},
                    kandidatMapper
            );
            return Optional.ofNullable(kandidat);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Kandidat> hentKandidater() {
        String query = lagKandidatQuery(false);
        return jdbcTemplate.query(query, kandidatMapper);
    }

    public List<HarTilretteleggingsbehov> hentHarTilretteleggingsbehov() {
        String query = lagKandidatQuery(true);
        return jdbcTemplate.query(query, harTilretteleggingsbehovMapper);
    }

    public Optional<HarTilretteleggingsbehov> hentHarTilretteleggingsbehov(String aktørId) {
        try {
            HarTilretteleggingsbehov harTilretteleggingsbehov = jdbcTemplate.queryForObject(
                    "SELECT * FROM kandidat WHERE (aktor_id = ?) ORDER BY registreringstidspunkt DESC LIMIT 1", new Object[]{aktørId},
                    harTilretteleggingsbehovMapper
            );

            return Optional.ofNullable(harTilretteleggingsbehov);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private String lagKandidatQuery(boolean inkluderSlettedeKandidater) {
        return (
                "SELECT k.* " +
                        "FROM kandidat k " +
                        "INNER JOIN " +
                        "(SELECT aktor_id, MAX(registreringstidspunkt) AS sisteRegistrert " +
                        "FROM kandidat " +
                        "GROUP BY aktor_id) gruppertKandidat " +
                        "ON k.aktor_id = gruppertKandidat.aktor_id " +
                        "AND k.registreringstidspunkt = gruppertKandidat.sisteRegistrert " +
                        (inkluderSlettedeKandidater ? "" : "WHERE slettet = false ") +
                        "ORDER BY k.registreringstidspunkt"
        );
    }

    public void slettAlleKandidater() {
        jdbcTemplate.execute("DELETE FROM kandidat");
    }

    public Integer lagreKandidat(Kandidat kandidat) {
        Map<String, Object> parameters = lagInsertParameter(kandidat);
        return jdbcInsert.executeAndReturnKey(parameters).intValue();
    }

    private Map<String, Object> lagInsertParameter(Kandidat kandidat) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(FNR, kandidat.getFnr());
        parameters.put(AKTØR_ID, kandidat.getAktørId());
        parameters.put(REGISTRERT_AV, kandidat.getSistEndretAv());
        parameters.put(REGISTRERT_AV_BRUKERTYPE, Brukertype.VEILEDER.name());
        parameters.put(REGISTRERINGSTIDSPUNKT, kandidat.getSistEndret());
        parameters.put(ARBEIDSTID_BEHOV, enumSetTilString(kandidat.getArbeidstidBehov()));
        parameters.put(FYSISKE_BEHOV, enumSetTilString(kandidat.getFysiskeBehov()));
        parameters.put(ARBEIDSMILJØ_BEHOV, enumSetTilString(kandidat.getArbeidsmiljøBehov()));
        parameters.put(GRUNNLEGGENDE_BEHOV, enumSetTilString(kandidat.getGrunnleggendeBehov()));
        parameters.put(NAV_KONTOR, kandidat.getNavKontor());
        parameters.put(SLETTET, false);
        parameters.put(OPPRETTET, LocalDateTime.now());

        return parameters;
    }

    public Optional<Integer> slettKandidatSomMaskinbruker(
            String aktørId,
            LocalDateTime slettetTidspunkt
    ) {

        Optional<Kandidat> kandidat = hentNyesteKandidat(aktørId);
        if (kandidat.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(AKTØR_ID, aktørId);
        parameters.put(REGISTRERT_AV, Brukertype.SYSTEM.name());
        parameters.put(REGISTRERT_AV_BRUKERTYPE, Brukertype.SYSTEM.name());
        parameters.put(REGISTRERINGSTIDSPUNKT, slettetTidspunkt);
        parameters.put(OPPRETTET, LocalDateTime.now());
        parameters.put(SLETTET, true);

        return Optional.ofNullable(jdbcInsert.executeAndReturnKey(parameters).intValue());
    }

    public Optional<Integer> slettKandidat(
            String aktørId,
            Veileder slettetAv,
            LocalDateTime slettetTidspunkt
    ) {
        Optional<Kandidat> kandidat = hentNyesteKandidat(aktørId);
        if (kandidat.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(AKTØR_ID, aktørId);
        parameters.put(REGISTRERT_AV, slettetAv.getNavIdent());
        parameters.put(REGISTRERT_AV_BRUKERTYPE, Brukertype.VEILEDER.name());
        parameters.put(REGISTRERINGSTIDSPUNKT, slettetTidspunkt);
        parameters.put(OPPRETTET, LocalDateTime.now());
        parameters.put(SLETTET, true);

        return Optional.ofNullable(jdbcInsert.executeAndReturnKey(parameters).intValue());
    }

    public int oppdaterNavKontor(String fnr, String navKontor) {
        if (fnr == null) {
            log.warn("Prøvde å oppdatere navkontor, men fnr er null");
            return 0;
        } else {
            try {
                return jdbcTemplate.update("UPDATE kandidat SET nav_kontor = ? WHERE fnr = ?", navKontor, fnr);
            } catch (DataAccessException e) {
                throw new FinnKandidatException("Klarte ikke å oppdatere rad med NAV-kontor");
            }
        }
    }
}
