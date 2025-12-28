package ru.practicum.ewm.stats.collector;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.collector.config.KafkaProperties;
import ru.practicum.ewm.stats.collector.config.TopicType;
import ru.practicum.ewm.stats.collector.util.YamlUtils;

import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
public class UserActionProducer implements AutoCloseable {
    private final KafkaProducer<String, SpecificRecordBase> kafkaProducer;
    private final Map<TopicType, String> topics;

    public UserActionProducer(KafkaProperties properties) {
        YamlUtils yamlUtils = new YamlUtils();
        Map<String, Object> props = yamlUtils.flatYaml(properties.getProducer());

        this.kafkaProducer = new KafkaProducer<>(props);
        this.topics = properties.getTopics();
    }

    public void send(SpecificRecordBase action, TopicType topic) {
        String topicName = topics.get(topic);
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topicName, null, action);

        log.info("<== Send message: {} to topic: {}", record, topic);
        kafkaProducer.send(record);
    }

    @Override
    public void close() {
        kafkaProducer.flush();
        kafkaProducer.close(Duration.ofSeconds(10));
    }
}
