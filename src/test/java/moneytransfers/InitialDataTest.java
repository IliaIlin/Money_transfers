package moneytransfers;

import moneytransfers.dto.TransferDto;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.get;
import static moneytransfers.TestUtils.*;
import static org.hamcrest.Matchers.*;

public class InitialDataTest {

    @BeforeAll
    static void beforeAll() {
        bootstrapMoneyTransferApplication();
    }

    @Test
    void initialBalancesAreOk() {
        get("/accounts/1/balance")
                .then()
                .statusCode(HttpStatus.OK_200)
                .body(equalTo("350.00"));

        get("/accounts/2/balance")
                .then()
                .statusCode(HttpStatus.OK_200)
                .body(equalTo("250.00"));

        get("/accounts/3/balance")
                .then()
                .statusCode(HttpStatus.OK_200)
                .body(equalTo("500.00"));

        get("/accounts/4/balance")
                .then()
                .statusCode(HttpStatus.OK_200)
                .body(equalTo("1000.00"));
    }

    @Test
    void initialTransfersAreOk() {
        assertGetTransfersResponse(
                HttpStatus.OK_200,
                List.of(
                        new TransferDto(1L, 4L, new BigDecimal("20.00"))
                ),
                "/accounts/1/transfers");

        assertGetTransfersResponse(
                HttpStatus.OK_200,
                new ArrayList<>(),
                "/accounts/2/transfers");

        assertGetTransfersResponse(
                HttpStatus.OK_200,
                List.of(
                        new TransferDto(3L, 4L, new BigDecimal("30.00"))
                ),
                "/accounts/3/transfers");

        assertGetTransfersResponse(
                HttpStatus.OK_200,
                List.of(
                        new TransferDto(1L, 4L, new BigDecimal("20.00")),
                        new TransferDto(3L, 4L, new BigDecimal("30.00"))
                ),
                "/accounts/4/transfers");
    }

    @AfterAll
    static void afterAll() {
        stopMoneyTransferApplication();
    }
}
