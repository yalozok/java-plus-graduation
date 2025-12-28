package ru.practicum.ewm.stats.analyzer;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.analyzer.service.InteractionService;
import ru.practicum.ewm.stats.analyzer.service.RecommendationService;
import ru.practicum.ewm.stats.proto.*;

import java.util.List;
import java.util.Map;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationController extends RecommendationControllerGrpc.RecommendationControllerImplBase {
    private final RecommendationService recommendationService;
    private final InteractionService interactionService;

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("==> Get interactions count");
            List<Long> eventIds = request.getEventIdList();
            Map<Long, Double> ratings = interactionService.getTotalEventsRating(eventIds);
            for (Map.Entry<Long, Double> entry : ratings.entrySet()) {
                double rating = ratings.getOrDefault(entry.getKey(), 0.0);

                responseObserver.onNext(RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(rating)
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.fromThrowable(e)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("==> Get similar events");
            Map<Long, Double> events = recommendationService.getSimilarEvents(
                    request.getEventId(), request.getUserId(), request.getMaxResults()
            );

            for (Map.Entry<Long, Double> entry : events.entrySet()) {
                double similarity = events.getOrDefault(entry.getKey(), 0.0);

                responseObserver.onNext(RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(similarity)
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.fromThrowable(e)
            ));
        }
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("==> Get recommendations for user");
            Map<Long, Double> events = recommendationService.getRecommendationsForUser(
                    request.getUserId(), request.getMaxResults());
            for (Map.Entry<Long, Double> entry : events.entrySet()) {
                double similarity = events.getOrDefault(entry.getKey(), 0.0);

                responseObserver.onNext(RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(similarity)
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.fromThrowable(e)
            ));
        }
    }

}
