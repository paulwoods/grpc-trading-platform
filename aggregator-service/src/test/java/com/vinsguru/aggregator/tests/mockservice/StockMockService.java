package com.vinsguru.aggregator.tests.mockservice;

import com.google.protobuf.Empty;
import com.vinsguru.common.Ticker;
import com.vinsguru.stock.PriceUpdate;
import com.vinsguru.stock.StockPriceRequest;
import com.vinsguru.stock.StockPriceResponse;
import com.vinsguru.stock.StockServiceGrpc;
import io.grpc.stub.StreamObserver;

public class StockMockService extends StockServiceGrpc.StockServiceImplBase {
    @Override
    public void getStockPrice(StockPriceRequest request, StreamObserver<StockPriceResponse> responseObserver) {
        var response = StockPriceResponse.newBuilder()
                .setPrice(15)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPriceUpdates(Empty request, StreamObserver<PriceUpdate> responseObserver) {
        for(int i = 1; i <=5; i++) {
            var priceUpdate = PriceUpdate.newBuilder().setPrice(i).setTicker(Ticker.AMAZON).buildPartial();
            responseObserver.onNext(priceUpdate);
        }
        responseObserver.onCompleted();
    }
}
