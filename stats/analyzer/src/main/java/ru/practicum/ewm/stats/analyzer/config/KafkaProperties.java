package ru.practicum.ewm.stats.analyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("analyzer.kafka")
public class KafkaProperties {
    private Map<String, Object> userActions;
    private Map<String, Object> eventsSimilarity;
    private Map<TopicType, String> topics;
}
