package ru.practicum.ewm.stats.aggregator.config;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class YamlUtils {

    public Map<String, Object> flatYaml(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();

        for (String key : source.keySet()) {
            Object value = source.get(key);
            if (value instanceof Map) {
                Map<String, Object> inner = flatYaml((Map<String, Object>) value);
                for (String innerKey : inner.keySet()) {
                    result.put(key + "." + innerKey, inner.get(innerKey));
                }
            } else {
                result.put(key, value);
            }
        }
        return result;
    }
}
