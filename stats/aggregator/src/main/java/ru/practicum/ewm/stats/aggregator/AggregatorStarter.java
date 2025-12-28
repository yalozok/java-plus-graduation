package ru.practicum.ewm.stats.aggregator;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.aggregator.config.KafkaProperties;
import ru.practicum.ewm.stats.aggregator.config.TopicType;
import ru.practicum.ewm.stats.aggregator.config.YamlUtils;
import ru.practicum.ewm.stats.aggregator.service.AggregatorService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.explore.with.me.logging.Loggable;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AggregatorStarter {
    private final KafkaConsumer<String, SpecificRecordBase> consumer;
    protected final KafkaProducer<String, SpecificRecordBase> producer;
    private final AggregatorService service;
    Map<TopicType, String> topics;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new ConcurrentHashMap<>();
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    public AggregatorStarter(AggregatorService service, KafkaProperties properties) {
        YamlUtils yamlUtils = new YamlUtils();
        Map<String, Object> producerProps = yamlUtils.flatYaml(properties.getProducer());
        Map<String, Object> consumerProps = yamlUtils.flatYaml(properties.getConsumer());

        this.service = service;
        this.producer = new KafkaProducer<>(producerProps);
        this.consumer = new KafkaConsumer<>(consumerProps);
        this.topics = properties.getTopics();
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook triggered. Waking up consumer...");
            consumer.wakeup();
        }));

        try {
            consumer.subscribe(List.of(topics.get(TopicType.USER_ACTIONS)));

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    UserActionAvro event = handleRecord(record);
                    log.info("Aggregator received event: {}", event);
                    List<EventSimilarityAvro> similarities = service.handleUserAction(event);
                    for (EventSimilarityAvro sim : similarities) {
                        log.info("Aggregator sending similarity: {}", sim);
                        send(sim);
                    }
                    TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                    currentOffsets.put(tp, new OffsetAndMetadata(record.offset() + 1));
                }
            }
        } catch (WakeupException e) {
            log.info("Consumer shutdown detected.");
        } catch (Exception e) {
            log.error("Unexpected error in consumer loop", e);
        } finally {
            try {
                producer.flush();
                if (!currentOffsets.isEmpty()) {
                    try {
                        consumer.commitSync(currentOffsets);
                        log.info("Final offset commit successful: {}", currentOffsets);
                    } catch (Exception e) {
                        log.error("Failed to commit offsets during shutdown: {}", currentOffsets, e);
                    }
                }
            } finally {
                log.info("Closing consumer and producer");
                consumer.close();
                producer.close();
            }
        }
    }

    @Loggable
    private UserActionAvro handleRecord(ConsumerRecord<String, SpecificRecordBase> record) {
        if (!(record.value() instanceof UserActionAvro)) {
            throw new IllegalArgumentException("Unexpected record type: " + record.value().getClass());
        }
        return (UserActionAvro) record.value();
    }

    @Loggable
    private void send(SpecificRecordBase similarity) {
        String topic = topics.get(TopicType.EVENTS_SIMILARITY);
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, null, similarity);
        producer.send(record);
    }
}
