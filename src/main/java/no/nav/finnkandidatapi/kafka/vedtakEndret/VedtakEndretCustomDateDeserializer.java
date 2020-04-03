package no.nav.finnkandidatapi.kafka.vedtakEndret;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class VedtakEndretCustomDateDeserializer extends StdDeserializer<LocalDateTime> {

    private DateTimeFormatter formatterMedT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS" );
    private DateTimeFormatter formatterUtenT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS" );
    private DateTimeFormatter formatterMerkelig =
            DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss" );

    public VedtakEndretCustomDateDeserializer() {
        this(null);
    }

    public VedtakEndretCustomDateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonparser, DeserializationContext context)
            throws IOException {
        String tekst = jsonparser.getText();
        try {
            return LocalDateTime.parse(tekst, formatterMedT);
        } catch (DateTimeParseException dtpe) {
            try {
                return LocalDateTime.parse(tekst, formatterUtenT);
            } catch (DateTimeParseException dtpe2) {
                try {
                    return LocalDateTime.parse(tekst, formatterMerkelig);
                } catch (DateTimeParseException dtpe3) {
                    throw new RuntimeException(String.format("Klarte ikke å parse %s, feil %s, %s og %s", tekst, dtpe.getMessage(), dtpe2.getMessage(), dtpe3.getMessage()));
                }
            }
        }
    }
}

