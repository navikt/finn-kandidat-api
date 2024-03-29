package no.nav.finnkandidatapi.kafka.vedtakReplikert;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VedtakReplikert {

    private String table;
    private String op_type;
    @JsonDeserialize(using = VedtakReplikertCustomDateDeserializer.class)
    private LocalDateTime op_ts;
    @JsonDeserialize(using = VedtakReplikertCustomDateDeserializer.class)
    private LocalDateTime current_ts;
    private String pos;
    private String fodselsnr;
    private VedtakRad before;
    private VedtakRad after;
}
