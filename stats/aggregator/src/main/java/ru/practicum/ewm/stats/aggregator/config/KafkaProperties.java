package ru.practicum.ewm.stats.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("aggregator.kafka")
public class KafkaProperties {
    private Map<String, Object> consumer;
    private Map<String, Object> producer;
    private Map<TopicType, String> topics;
}
