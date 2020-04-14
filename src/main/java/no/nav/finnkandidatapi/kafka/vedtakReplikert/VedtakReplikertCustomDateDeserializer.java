package no.nav.finnkandidatapi.kafka.vedtakReplikert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class VedtakReplikertCustomDateDeserializer extends StdDeserializer<LocalDateTime> {

    private DateTimeFormatter formatterMedTOgMikroSekunder =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS" );
    private DateTimeFormatter formatterUtenTOgMedMikroSekunder =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS" );
    private DateTimeFormatter formatterUtenT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss" );
    private DateTimeFormatter formatterMedT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss" );
    private DateTimeFormatter formatterMerkelig =
            DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss" );

    public VedtakReplikertCustomDateDeserializer() {
        this(null);
    }

    public VedtakReplikertCustomDateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonparser, DeserializationContext context)
            throws IOException {
        String tekst = jsonparser.getText();
        if (tekst == null || tekst.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(tekst, formatterMedTOgMikroSekunder);
        } catch (DateTimeParseException dtpe) {
            try {
                return LocalDateTime.parse(tekst, formatterUtenTOgMedMikroSekunder);
            } catch (DateTimeParseException dtpe2) {
                try {
                    return LocalDateTime.parse(tekst, formatterUtenT);
                } catch (DateTimeParseException dtpe3) {
                    try {
                        return LocalDateTime.parse(tekst, formatterMedT);
                    } catch (DateTimeParseException dtpe4) {
                        try {
                            return LocalDateTime.parse(tekst, formatterMerkelig);
                        } catch (DateTimeParseException dtpe5) {
                            throw new RuntimeException(String.format("Klarte ikke å parse %s, siste feil  er %s", tekst, dtpe5.getMessage()));
                        }
                    }
                }
            }
        }
    }
}

