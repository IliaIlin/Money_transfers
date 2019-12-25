package moneytransfers;

import moneytransfers.dto.TransferDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import static moneytransfers.TestUtils.*;
import static moneytransfers.TestUtils.GET;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrencyTest {

    @BeforeAll
    static void beforeAll() {
        bootstrapMoneyTransferApplication();
    }

    @Test
    void transfer_5simultaneousRequests_resultShouldBePredictable() throws InterruptedException, IOException {
        CountDownLatch countDownLatch = new CountDownLatch(5);

        new Thread(() -> {
            getResponseSuppressingException("/transfer", POST, new TransferDto(4L, 1L, new BigDecimal("50.00")));
            countDownLatch.countDown();
        }).start();

        new Thread(() -> {
            getResponseSuppressingException("/transfer", POST, new TransferDto(2L, 3L, new BigDecimal("40.00")));
            countDownLatch.countDown();
        }).start();

        new Thread(() -> {
            getResponseSuppressingException("/transfer", POST, new TransferDto(1L, 4L, new BigDecimal("10.00")));
            countDownLatch.countDown();
        }).start();

        new Thread(() -> {
            getResponseSuppressingException("/transfer", POST, new TransferDto(3L, 2L, new BigDecimal("10.00")));
            countDownLatch.countDown();
        }).start();

        new Thread(() -> {
            getResponseSuppressingException("/transfer", POST, new TransferDto(3L, 2L, new BigDecimal("10.00")));
            countDownLatch.countDown();
        }).start();

        countDownLatch.await();

        assertEquals("390.00", getResponse("/accounts/1/balance", GET));
        assertEquals("230.00", getResponse("/accounts/2/balance", GET));
        assertEquals("520.00", getResponse("/accounts/3/balance", GET));
        assertEquals("960.00", getResponse("/accounts/4/balance", GET));
    }

    @AfterAll
    static void afterAll() {
        stopMoneyTransferApplication();
    }
}
