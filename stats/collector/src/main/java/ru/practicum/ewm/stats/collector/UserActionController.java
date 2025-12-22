package ru.practicum.ewm.stats.collector;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    UserActionService actionService;

    @Override
    public void collectUserAction(UserActionProto userAction, StreamObserver<Empty> responseObserver) {
        try {
            log.info("==> Add user action: {}", userAction);
            actionService.handle(userAction);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.fromThrowable(e)
            ));
        }
    }
}
