package no.nav.finnkandidatapi.kafka.samtykke;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ExtractNumbersDeserializer extends JsonDeserializer<String> {
    public ExtractNumbersDeserializer() {
        super();
    }

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String text = jsonParser.getText();
        if(text == null) {
            return null;
        }
        return jsonParser.getText().replaceAll("\\D+", "");
    }
}
