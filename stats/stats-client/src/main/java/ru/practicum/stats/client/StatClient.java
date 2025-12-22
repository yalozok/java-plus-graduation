package ru.practicum.stats.client;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.RecommendationControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.explore.with.me.logging.Loggable;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StatClient {
    @GrpcClient("analyzer")
    private RecommendationControllerGrpc.RecommendationControllerBlockingStub recommendationClient;

    @GrpcClient("controller")
    private UserActionControllerGrpc.UserActionControllerBlockingStub actionClient;

    @Loggable
    public void sendViewAction(long userId, long eventId) {
        UserActionProto action = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setTimestamp(toProtoTimestamp(Instant.now()))
                .setActionType(ActionTypeProto.ACTION_VIEW)
                .build();
        try {
            actionClient.collectUserAction(action);
        } catch (StatusRuntimeException e) {
            throw new StatsClientException("Failed to send VIEW action for user" + userId, e);
        }
    }

    @Loggable
    public void sendRegisterAction(long userId, long eventId) {
        UserActionProto action = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setTimestamp(toProtoTimestamp(Instant.now()))
                .setActionType(ActionTypeProto.ACTION_REGISTER)
                .build();
        try {
            actionClient.collectUserAction(action);
        } catch (StatusRuntimeException e) {
            throw new StatsClientException("Failed to send REGISTER action for user" + userId, e);
        }
    }

    @Loggable
    public void sendLikeAction(long userId, long eventId) {
        UserActionProto action = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setTimestamp(toProtoTimestamp(Instant.now()))
                .setActionType(ActionTypeProto.ACTION_LIKE)
                .build();
        try {
            actionClient.collectUserAction(action);
        } catch (StatusRuntimeException e) {
            throw new StatsClientException("Failed to send LIKE action for user" + userId, e);
        }
    }

    private Timestamp toProtoTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}