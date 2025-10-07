package com.vinsguru.user.service;

import com.vinsguru.user.*;
import com.vinsguru.user.service.handler.UserInformationRequestHandler;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class UserService extends UserServiceGrpc.UserServiceImplBase {

    private final UserInformationRequestHandler userInformationRequestHandler;

    public UserService(UserInformationRequestHandler userInformationRequestHandler) {
        this.userInformationRequestHandler = userInformationRequestHandler;
    }

    @Override
    public void getUserInformation(UserInformationRequest request, StreamObserver<UserInformation> responseObserver) {
        var userInformation = userInformationRequestHandler.getUserInformation(request);
        responseObserver.onNext(userInformation);
        responseObserver.onCompleted();
    }

    @Override
    public void tradeStock(StockTradeRequest request, StreamObserver<StockTradeResponse> responseObserver) {
    }

}
