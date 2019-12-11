package money_transfers;

import money_transfers.dto.TransferDto;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static money_transfers.MoneyTransferApplication.*;
import static money_transfers.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MoneyTransferApplicationTest {

    @BeforeAll
    static void beforeAll() {
        bootstrapMoneyTransferApplication();
    }

    @Test
    @Order(1)
    void initialBalancesAreOk() throws IOException {
        assertEquals("350.00", getResponse("/accounts/1/balance", GET));
        assertEquals("250.00", getResponse("/accounts/2/balance", GET));
        assertEquals("500.00", getResponse("/accounts/3/balance", GET));
        assertEquals("1000.00", getResponse("/accounts/4/balance", GET));
    }

    @Test
    @Order(2)
    void initialTransfersAreOk() throws IOException {
        assertTransfersResponse(
                List.of(new TransferDto(1L, 4L, new BigDecimal("20.00"))),
                getResponse("/accounts/1/transfers", GET));

        assertTransfersResponse(
                new ArrayList<>(),
                getResponse("/accounts/2/transfers", GET));

        assertTransfersResponse(
                List.of(new TransferDto(3L, 4L, new BigDecimal("30.00"))),
                getResponse("/accounts/3/transfers", GET));

        assertTransfersResponse(
                List.of(new TransferDto(1L, 4L, new BigDecimal("20.00")),
                        new TransferDto(3L, 4L, new BigDecimal("30.00"))),
                getResponse("/accounts/4/transfers", GET));
    }

    @Test
    @Order(3)
    void getBalance_notExistingAccountId_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(INTERNAL_SERVER_ERROR_MESSAGE, getResponse("/accounts/100/balance", GET));
    }

    @Test
    @Order(4)
    void getBalance_notNumericAccountId_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(INTERNAL_SERVER_ERROR_MESSAGE, getResponse("/accounts/str/balance", GET));
    }

    @Test
    @Order(5)
    void getTransfers_notExistingAccountId_shouldReturnEmptyList() throws IOException {
        assertTransfersResponse(new ArrayList<>(), getResponse("/accounts/15/transfers", GET));
    }

    @Test
    @Order(6)
    void transfer_notExistingAccountId_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(TRANSFER_EXECUTION_ERROR_MESSAGE,
                getResponse("/transfer", POST, new TransferDto(10L, 4L, new BigDecimal("20.00"))));
    }

    @Test
    @Order(7)
    void transfer_notSufficientFunds_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(TRANSFER_EXECUTION_ERROR_MESSAGE,
                getResponse("/transfer", POST, new TransferDto(1L, 4L, new BigDecimal("1000.00"))));
    }


    @Test
    @Order(8)
    void transfer_unspecifiedAmount_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(INVALID_REQUEST_BODY_MESSAGE,
                getResponse("/transfer", POST, new TransferDto(1L, 4L, null)));
    }

    @Test
    @Order(9)
    void transfer_invalidRequestBody_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(INVALID_REQUEST_BODY_MESSAGE,
                getResponse("/transfer", POST, null));
    }

    @Test
    @Order(10)
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

}
