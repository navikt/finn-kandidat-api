package no.nav.tag.finnkandidatapi.tilgangskontroll.abac;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import no.nav.tag.finnkandidatapi.tilgangskontroll.abac.response.XacmlResponse;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbacClientTest {
    @Test
    @SneakyThrows
    public void test() {
        String str = "{\n" +
                "  \"Response\" : {\n" +
                "    \"Decision\" : \"Permit\",\n" +
                "    \"Status\" : {\n" +
                "      \"StatusCode\" : {\n" +
                "        \"Value\" : \"urn:oasis:names:tc:xacml:1.0:status:ok\",\n" +
                "        \"StatusCode\" : {\n" +
                "          \"Value\" : \"urn:oasis:names:tc:xacml:1.0:status:ok\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"AssociatedAdvice\" : {\n" +
                "      \"Id\" : \"no.nav.abac.advices.ping.response\",\n" +
                "      \"AttributeAssignment\" : {\n" +
                "        \"AttributeId\" : \"no.nav.abac.attributter.adviceorobligation.fritekst\",\n" +
                "        \"Value\" : \"ping response\",\n" +
                "        \"Category\" : \"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\",\n" +
                "        \"DataType\" : \"http://www.w3.org/2001/XMLSchema#string\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        ObjectMapper objectMapper = new ObjectMapper();
        XacmlResponse response = objectMapper.readValue(str, XacmlResponse.class);
    }
}