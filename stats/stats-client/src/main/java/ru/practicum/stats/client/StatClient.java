package ru.practicum.stats.client;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.explore.with.me.logging.Loggable;

import java.time.Instant;
import java.util.*;

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
            throw new StatsClientException("Failed to send VIEW action for user: " + userId, e);
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
            throw new StatsClientException("Failed to send REGISTER action for user: " + userId, e);
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
            throw new StatsClientException("Failed to send LIKE action for user: " + userId, e);
        }
    }

    private Timestamp toProtoTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    @Loggable
    public Map<Long, Double> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto
                .newBuilder()
                .addAllEventId(eventIds)
                .build();
        try {
            Iterator<RecommendedEventProto> iterator = recommendationClient.getInteractionsCount(request);
            Map<Long, Double> result = new HashMap<>();

            while (iterator.hasNext()) {
                RecommendedEventProto proto = iterator.next();
                result.put(proto.getEventId(), proto.getScore());
            }
            return result;
        } catch (Exception e) {
            throw new StatsClientException("Failed to get rating for events", e);
        }
    }

    @Loggable
    public Map<Long, Double> getSimilarEvents(long userId, long eventId, int limit) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto
                .newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(limit)
                .build();
        try {
            Iterator<RecommendedEventProto> iterator = recommendationClient.getSimilarEvents(request);
            Map<Long, Double> result = new HashMap<>();

            while (iterator.hasNext()) {
                RecommendedEventProto proto = iterator.next();
                result.put(proto.getEventId(), proto.getScore());
            }
            return result;
        } catch (Exception e) {
            throw new StatsClientException("Failed to get similar events for eventId " + eventId, e);
        }
    }

    @Loggable
    public Map<Long, Double> getRecommendationsForUser(long userId, int limit) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto
                .newBuilder()
                .setUserId(userId)
                .setMaxResults(limit)
                .build();
        try {
            Iterator<RecommendedEventProto> iterator = recommendationClient.getRecommendationsForUser(request);
            Map<Long, Double> result = new HashMap<>();

            while (iterator.hasNext()) {
                RecommendedEventProto proto = iterator.next();
                result.put(proto.getEventId(), proto.getScore());
            }
            return result;
        } catch (Exception e) {
            throw new StatsClientException("Failed to get recommendations for user: " + userId, e);
        }
    }
}