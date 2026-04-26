package ru.practicum.ewm.stats.collector;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.collector.config.TopicType;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Service
@RequiredArgsConstructor
public class UserActionService {
    private final UserActionMapper mapper;
    private final UserActionProducer producer;

    public void handle(UserActionProto proto) {
        UserActionAvro avro = mapper.toAvro(proto);
        producer.send(avro, TopicType.USER_ACTIONS);
    }
}
