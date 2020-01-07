package no.nav.tag.finnkandidatapi.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.tag.finnkandidatapi.kandidat.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static no.nav.tag.finnkandidatapi.TestData.enKandidatDto;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"local", "mock"})
@DirtiesContext
public class OpprettKandidatTest {
    @Autowired
    private EnKafkaMockServer embeddedKafka;

    private Consumer<String, String> consumer;

    private TestRestTemplate restTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);

    @LocalServerPort
    private int port;

    private String localBaseUrl() {
        return "http://localhost:" + port + "/finn-kandidat-api";
    }

    @Before
    public void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = cf.createConsumer();
        embeddedKafka.getEmbeddedKafka().consumeFromAnEmbeddedTopic(consumer, "aapen-tag-kandidatEndret-v1-default");

        String loginUrl = localBaseUrl() + "/local/isso-login";
        restTemplate.getForObject(loginUrl, String.class);

    }

    @Test
    public void nårMottarHttpRequest_skalSendeKafkaMelding() throws JsonProcessingException {
        // Given
        URI uri = URI.create(localBaseUrl() + "/kandidater");
        KandidatDto dto = enKandidatDto();
        dto.setAktørId("1856024171652");

        // When
        restTemplate.postForEntity(uri, dto, String.class);

        // Then
        ConsumerRecord<String, String> melding = KafkaTestUtils.getSingleRecord(consumer, "aapen-tag-kandidatEndret-v1-default", 2000L);
        HarTilretteleggingsbehov actualTilretteleggingsbehov = new ObjectMapper().readValue(melding.value(), HarTilretteleggingsbehov.class);
        List<String> actualBehov = actualTilretteleggingsbehov.getBehov();
        final Set<String> expectedBehov = Set.of(
                ArbeidsmiljøBehov.behovskategori,
                FysiskBehov.behovskategori,
                GrunnleggendeBehov.behovskategori
        );
        assertThat(actualTilretteleggingsbehov.getAktoerId()).isEqualTo(dto.getAktørId());
        assertThat(actualTilretteleggingsbehov.isHarTilretteleggingsbehov()).isTrue();
        assertThat(actualBehov).containsAll(expectedBehov);
        assertThat(actualBehov).hasSameSizeAs(expectedBehov);
    }
}
