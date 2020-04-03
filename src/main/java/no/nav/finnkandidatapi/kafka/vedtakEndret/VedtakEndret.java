package no.nav.finnkandidatapi.kafka.vedtakEndret;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VedtakEndret {

    /* Eksempel på GG-melding:

    {
    "table":"GG.TCUSTORD",
    "op_type":"U",
    "op_ts":"2013-06-02 22:14:41.000000",
    "current_ts":"2015-09-18T13:39:35.748000",
    "pos":"00000000000000002891",
    "tokens":{
        "R":"AADPkvAAEAAEqLzAAA"
    },
    "before":{
        "CUST_CODE":"BILL",
        "ORDER_DATE":"1995-12-31:15:00:00",
        "PRODUCT_CODE":"CAR",
        "ORDER_ID":"765",
        "PRODUCT_PRICE":15000.00,
        "PRODUCT_AMOUNT":3,
        "TRANSACTION_ID":"100"
    },
    "after":{
        "CUST_CODE":"BILL",
        "ORDER_DATE":"1995-12-31:15:00:00",
        "PRODUCT_CODE":"CAR",
        "ORDER_ID":"765",
        "PRODUCT_PRICE":14000.00,
        "PRODUCT_AMOUNT":3,
        "TRANSACTION_ID":"100"
    }
}
     */
    private String table;
    private String op_type;
    @JsonDeserialize(using = VedtakEndretCustomDateDeserializer.class)
    private LocalDateTime op_ts;
    @JsonDeserialize(using = VedtakEndretCustomDateDeserializer.class)
    private LocalDateTime current_ts;
    private String pos;
    private VedtakRad before;
    private VedtakRad after;
}
