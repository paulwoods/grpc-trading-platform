package com.vinsguru.user.tests;

import com.vinsguru.common.Ticker;
import com.vinsguru.user.StockTradeRequest;
import com.vinsguru.user.TradeAction;
import com.vinsguru.user.UserInformationRequest;
import com.vinsguru.user.UserServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "grpc.server.port=-1",
        "grpc.server.in-process-name=integration-test",
        "grpc.client.user-service.address=in-process:integration-test"
})
public class UserServiceTest {

    @SuppressWarnings("unused")
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub stub;

    @Test
    public void userInformationTest() {
        var request = UserInformationRequest.newBuilder()
                .setUserId(1)
                .build();

        var response = stub.getUserInformation(request);

        assertEquals(1, response.getUserId());
        assertEquals(10_000, response.getBalance());
        assertEquals("Sam", response.getName());
        assertTrue(response.getHoldingsList().isEmpty());
    }

    @Test
    public void unknownUserTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {

            var request = UserInformationRequest.newBuilder()
                    .setUserId(12345)
                    .build();

            stub.getUserInformation(request);
        });

        assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
        assertEquals("User [id=12345] is not found", ex.getStatus().getDescription());
    }

    @Test
    public void unknownTickerBuyTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {

            var request = StockTradeRequest.newBuilder()
                    .setUserId(1)
                    .setPrice(1)
                    .setQuantity(1)
                    .setAction(TradeAction.BUY)
                    .build();

            stub.tradeStock(request);
        });

        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        assertEquals("Ticker is not found", ex.getStatus().getDescription());
    }

    @Test
    public void insufficientSharesTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {

            var request = StockTradeRequest.newBuilder()
                    .setUserId(1)
                    .setTicker(Ticker.AMAZON)
                    .setPrice(1)
                    .setQuantity(1000)
                    .setAction(TradeAction.SELL)
                    .build();

            stub.tradeStock(request);
        });

        assertEquals(Status.Code.FAILED_PRECONDITION, ex.getStatus().getCode());
        assertEquals("User [id=1] does not have enough shares to complete the transaction.", ex.getStatus().getDescription());
    }

    @Test
    public void insufficientBalanceTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {

            var request = StockTradeRequest.newBuilder()
                    .setUserId(1)
                    .setTicker(Ticker.AMAZON)
                    .setPrice(10_001)
                    .setQuantity(1)
                    .setAction(TradeAction.BUY)
                    .build();

            stub.tradeStock(request);
        });

        assertEquals(Status.Code.FAILED_PRECONDITION, ex.getStatus().getCode());
        assertEquals("User [id=1] does not have enough funds to complete the transaction.", ex.getStatus().getDescription());
    }

}
