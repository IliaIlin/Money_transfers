package moneytransfers;

import moneytransfers.dto.TransferDto;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.with;
import static moneytransfers.TestUtils.bootstrapMoneyTransferApplication;
import static moneytransfers.TestUtils.stopMoneyTransferApplication;
import static org.hamcrest.Matchers.equalTo;

public class ConcurrencyTest {
    private CountDownLatch countDownLatch;

    @BeforeAll
    static void beforeAll() {
        bootstrapMoneyTransferApplication();
    }

    @Test
    void transfer_5simultaneousRequests_resultShouldBePredictable() throws InterruptedException, IOException {
        countDownLatch = new CountDownLatch(5);

        executeTransferInNewThread(new TransferDto(4L, 1L, new BigDecimal("50.00")));
        executeTransferInNewThread(new TransferDto(2L, 3L, new BigDecimal("40.00")));
        executeTransferInNewThread(new TransferDto(1L, 4L, new BigDecimal("10.00")));
        executeTransferInNewThread(new TransferDto(3L, 2L, new BigDecimal("10.00")));
        executeTransferInNewThread(new TransferDto(3L, 2L, new BigDecimal("10.00")));

        countDownLatch.await();

        get("/accounts/1/balance")
                .then()
                .statusCode(HttpStatus.OK_200)
                .body(equalTo("390.00"));

        get("/accounts/2/balance")
                .then()
                .statusCode(HttpStatus.OK_200)
                .body(equalTo("230.00"));

        get("/accounts/3/balance")
                .then()
                .statusCode(HttpStatus.OK_200)
                .body(equalTo("520.00"));

        get("/accounts/4/balance")
                .then()
                .statusCode(HttpStatus.OK_200)
                .body(equalTo("960.00"));
    }

    private void executeTransferInNewThread(TransferDto transferData) {
        new Thread(() -> {
            with()
                    .body(transferData)
                    .post("/transfer");
            countDownLatch.countDown();
        }).start();
    }

    @AfterAll
    static void afterAll() {
        stopMoneyTransferApplication();
    }
}
