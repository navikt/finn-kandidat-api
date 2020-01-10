package no.nav.tag.finnkandidatapi.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class KafkaTestUtil {

    static List<String> readKafkaMessages(final EnKafkaMockServer embeddedKafka, final int minExpectedMsgs) {
        return readKafkaMessages(embeddedKafka, minExpectedMsgs, Duration.ofSeconds(10));
    }

    static List<String> readKafkaMessages(final EnKafkaMockServer embeddedKafka, final int minExpectedMsgs, final Duration maxWaitDuration) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.toString().toLowerCase());

        final List<String> receivedMessages = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(minExpectedMsgs);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            KafkaConsumer<Integer, String> kafkaConsumer = new KafkaConsumer<>(consumerProps);
            kafkaConsumer.subscribe(Collections.singletonList(embeddedKafka.topicName));
            try {
                while (true) {
                    ConsumerRecords<Integer, String> records = kafkaConsumer.poll(Duration.ofMillis(100L));
                    records.iterator().forEachRemaining(record -> {
                        receivedMessages.add(record.value());
                        latch.countDown();
                    });
                }
            } finally {
                kafkaConsumer.close();
            }
        });

        final long maxWaitSeconds = maxWaitDuration.toSeconds() <= 0 ? 1 : maxWaitDuration.toSeconds();
        final boolean waitTimeIsExhausted;
        try {
            waitTimeIsExhausted = !latch.await(maxWaitSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (waitTimeIsExhausted) {
            String msg = "Only " + receivedMessages.size() + " of expected " + minExpectedMsgs + " messages received within the given duration of " + maxWaitSeconds + " seconds.";
            throw new AssertionError(msg);
        } else {
            return Collections.unmodifiableList(receivedMessages);
        }
    }
}
