package ru.practicum.ewm.stats.collector.util;

import com.google.protobuf.Timestamp;

import java.time.Instant;

public class ProtoTimeUtil {
    public static Timestamp toProtoTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
