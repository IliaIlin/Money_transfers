package moneytransfers;

import moneytransfers.dto.TransferDto;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static io.restassured.RestAssured.*;
import static moneytransfers.TestUtils.*;
import static moneytransfers.controller.MoneyTransferController.*;
import static org.hamcrest.Matchers.equalTo;

public class ExceptionsTest {

    @BeforeAll
    static void beforeAll() {
        bootstrapMoneyTransferApplication();
    }

    @Test
    void getBalance_notExistingAccountId_shouldReturnCorrectErrorMessage() {
        get("/accounts/100/balance")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .body(equalTo(INTERNAL_SERVER_ERROR_MESSAGE));
    }

    @Test
    void getBalance_notNumericAccountId_shouldReturnCorrectErrorMessage() {
        get("/accounts/str/balance")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .body(equalTo(INTERNAL_SERVER_ERROR_MESSAGE));
    }

    @Test
    void getTransfers_notExistingAccountId_shouldReturnEmptyList() {
        assertGetTransfersResponse(HttpStatus.OK_200, new ArrayList<>(), "/accounts/15/transfers");
    }

    @Test
    void transfer_notExistingAccountId_shouldReturnCorrectErrorMessage() {
        with()
                .body(new TransferDto(10L, 4L, new BigDecimal("20.00")))
                .post("/transfer")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(TRANSFER_EXECUTION_ERROR_MESSAGE));
    }

    @Test
    void transfer_notSufficientFunds_shouldReturnCorrectErrorMessage() {
        with()
                .body(new TransferDto(1L, 4L, new BigDecimal("1000.00")))
                .post("/transfer")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(TRANSFER_EXECUTION_ERROR_MESSAGE));
    }

    @Test
    void transfer_unspecifiedAmount_shouldReturnCorrectErrorMessage() {
        with()
                .body(new TransferDto(1L, 4L, null))
                .post("/transfer")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(INVALID_REQUEST_BODY_MESSAGE));
    }

    @Test
    void transfer_invalidRequestBody_shouldReturnCorrectErrorMessage() {
        post("/transfer")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(INVALID_REQUEST_BODY_MESSAGE));
    }

    @Test
    void transfer_negativeAmountOfMoney_shouldReturnCorrectErrorMessage() {
        with()
                .body(new TransferDto(1L, 4L, new BigDecimal("-100.00")))
                .post("/transfer")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(INVALID_REQUEST_BODY_MESSAGE));
    }

    @AfterAll
    static void afterAll() {
        stopMoneyTransferApplication();
    }
}
