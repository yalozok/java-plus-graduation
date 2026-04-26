package ru.practicum.ewm.stats.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.config.KafkaProperties;
import ru.practicum.ewm.stats.analyzer.config.TopicType;
import ru.practicum.ewm.stats.analyzer.config.YamlUtils;
import ru.practicum.ewm.stats.analyzer.service.InteractionService;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.explore.with.me.logging.Loggable;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class UserActionConsumer implements Runnable {
    private final InteractionService interactionService;
    private final KafkaConsumer<String, SpecificRecordBase> consumer;
    Map<TopicType, String> topics;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new ConcurrentHashMap<>();
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);

    public UserActionConsumer(InteractionService interactionService,
                              KafkaProperties props) {
        YamlUtils yamlUtils = new YamlUtils();
        Map<String, Object> propsMap = yamlUtils.flatYaml(props.getUserActions());

        this.interactionService = interactionService;
        this.consumer = new KafkaConsumer<>(propsMap);
        this.topics = props.getTopics();
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook triggered. Waking up user action consumer...");
            consumer.wakeup();
        }));

        try {
            consumer.subscribe(List.of(topics.get(TopicType.USER_ACTIONS)));
            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    UserActionAvro action = handleRecord(record);
                    interactionService.saveUserAction(action);
                    TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                    currentOffsets.put(tp, new OffsetAndMetadata(record.offset() + 1));
                }
            }
        } catch (WakeupException e) {
            log.info("User action consumer shutdown detected.");
        } catch (Exception e) {
            log.error("Unexpected error in User action consumer loop", e);
        } finally {
            try {
                if (!currentOffsets.isEmpty()) {
                    try {
                        consumer.commitSync(currentOffsets);
                        log.info("Final offset commit successful: {}", currentOffsets);
                    } catch (Exception e) {
                        log.error("Failed to commit offsets during shutdown: {}", currentOffsets, e);
                    }
                }
            } finally {
                log.info("Closing consumer");
                consumer.close();
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
}
