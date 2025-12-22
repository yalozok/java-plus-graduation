package ru.practicum.ewm.stats.collector;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.collector.kafka.TopicType;
import ru.practicum.ewm.stats.collector.kafka.UserActionProducer;
import ru.practicum.ewm.stats.collector.util.ProtoTimeUtil;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Service
@RequiredArgsConstructor
public class UserActionService {
    private final UserActionMapper mapper;
    private final UserActionProducer producer;

    public void handle(UserActionProto proto) {
        UserActionAvro avro = mapper.toAvro(proto);
        String userId = String.valueOf(proto.getUserId());

        producer.send(
                avro,
                userId,
                ProtoTimeUtil.toInstant(proto.getTimestamp()),
                TopicType.USER_ACTIONS
        );
    }
}
