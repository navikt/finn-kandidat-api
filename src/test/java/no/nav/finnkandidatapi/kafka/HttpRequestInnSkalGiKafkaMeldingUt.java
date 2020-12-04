package no.nav.finnkandidatapi.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.finnkandidatapi.kafka.harTilretteleggingsbehov.HarTilretteleggingsbehov;
import no.nav.finnkandidatapi.kandidat.*;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import no.nav.security.token.support.test.JwtTokenGenerator;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.time.Duration;
import java.util.*;

import static no.nav.finnkandidatapi.TestData.*;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.consumer.OffsetResetStrategy.EARLIEST;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"local", "mock"})
@EnableMockOAuth2Server
@DirtiesContext
public class HttpRequestInnSkalGiKafkaMeldingUt {

    @Autowired
    private EnKafkaMockServer embeddedKafka;

    private TestRestTemplate restTemplate = new TestRestTemplate(TestRestTemplate.HttpClientOption.ENABLE_COOKIES);

    @LocalServerPort
    private int port;

    private String localBaseUrl() {
        return "http://localhost:" + port + "/finn-kandidat-api";
    }

    private KafkaConsumer<Integer, String> kafkaConsumer;

    @Before
    public void setUp() {
        URI loginUrl = URI.create(localBaseUrl() + "/jejjo/egenmekka-cookie");
        // TODO: Denne funker ikke av en eller annen grunn
        ResponseEntity<String> respons = restTemplate.getForEntity(loginUrl, String.class);
        assertThat(respons.getHeaders().get("Set-Cookie")).isNotNull();

//        JwtTokenGenerator.createSignedJWT("");

        kafkaConsumer = setupKafkaConsumer();
    }

    @Test
    public void nårMottarHttpRequestOpprettEndreSlett_skalSendeKafkaMeldingOpprettEndreSlett() throws JsonProcessingException {
        // Given
        URI uri = URI.create(localBaseUrl() + "/kandidater");
        KandidatDto dto = enKandidatDto();
        dto.setAktørId("1856024171652");

//        System.out.println(token("isso", "12312312", "aud-isso"));

        // TODO Er ikke logga inn?
        // When HTTP opprett
        ResponseEntity<String> respons = restTemplate.postForEntity(uri, dto, String.class);
        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.CREATED);


        // Then Kafka opprett
        final List<String> opprettMsgs = readKafkaMessages();
        assertThat(opprettMsgs).isNotEmpty();
        assertThat(opprettMsgs.size()).isEqualTo(List.of("opprett").size());
        HarTilretteleggingsbehov actualTilretteleggingsbehov = new ObjectMapper().readValue(opprettMsgs.get(0), HarTilretteleggingsbehov.class);
        List<String> actualBehov = actualTilretteleggingsbehov.getBehov();
        Set<String> expectedBehov = Set.of(
                Arbeidstid.behovskategori,
                Arbeidshverdagen.behovskategori,
                Fysisk.behovskategori,
                UtfordringerMedNorsk.behovskategori
        );
        assertThat(actualTilretteleggingsbehov.getAktoerId()).isEqualTo(dto.getAktørId());
        assertThat(actualTilretteleggingsbehov.isHarTilretteleggingsbehov()).isTrue();
        assertThat(actualBehov).containsAll(expectedBehov);
        assertThat(actualBehov).hasSameSizeAs(expectedBehov);

        // When HTTP endre
        assertThat(dto.getArbeidshverdagen()).isNotEmpty();
        dto.setArbeidshverdagen(Set.of());
        restTemplate.put(uri, dto);

        // Then Kafka endre
        final List<String> endreMsgs = readKafkaMessages();
        assertThat(endreMsgs).isNotEmpty();
        assertThat(endreMsgs.size()).isEqualTo(List.of("endre").size());
        actualTilretteleggingsbehov = new ObjectMapper().readValue(endreMsgs.get(0), HarTilretteleggingsbehov.class);
        actualBehov = actualTilretteleggingsbehov.getBehov();
        expectedBehov = Set.of(
                Arbeidstid.behovskategori,
                Fysisk.behovskategori,
                UtfordringerMedNorsk.behovskategori
        );
        assertThat(actualTilretteleggingsbehov.getAktoerId()).isEqualTo(dto.getAktørId());
        assertThat(actualTilretteleggingsbehov.isHarTilretteleggingsbehov()).isTrue();
        assertThat(actualBehov).containsAll(expectedBehov);
        assertThat(actualBehov).hasSameSizeAs(expectedBehov);

        // When HTTP slett
        URI deleteUri = URI.create(uri.toString() + "/" + dto.getAktørId());
        restTemplate.delete(deleteUri);

        // Then Kafka slett
        final List<String> slettMsgs = readKafkaMessages();
        assertThat(slettMsgs).isNotEmpty();
        assertThat(slettMsgs.size()).isEqualTo(List.of("slett").size());
        actualTilretteleggingsbehov = new ObjectMapper().readValue(slettMsgs.get(0), HarTilretteleggingsbehov.class);
        assertThat(actualTilretteleggingsbehov.getAktoerId()).isEqualTo(dto.getAktørId());
        assertThat(actualTilretteleggingsbehov.isHarTilretteleggingsbehov()).isFalse();
        assertThat(actualTilretteleggingsbehov.getBehov()).isEmpty();
    }

    @After
    public void tearDown() {
        kafkaConsumer.close();
    }

    private KafkaConsumer<Integer, String> setupKafkaConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(AUTO_OFFSET_RESET_CONFIG, EARLIEST.toString().toLowerCase());
        KafkaConsumer<Integer, String> kafkaConsumer = new KafkaConsumer<>(consumerProps);
        kafkaConsumer.subscribe(Collections.singletonList(embeddedKafka.topicName));
        return kafkaConsumer;
    }

    private List<String> readKafkaMessages() {
        final List<String> messages = new ArrayList<>();
        // TODO ta tilbake slik det var
        ConsumerRecords<Integer, String> records = KafkaTestUtils.getRecords(kafkaConsumer, Duration.ofSeconds(10).toMillis());
        records.forEach(record -> messages.add(record.value()));
        return Collections.unmodifiableList(messages);
    }
}
