package moneytransfers;

import moneytransfers.dto.TransferDto;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static moneytransfers.TestUtils.*;

public class InitialDataTest {

    @BeforeAll
    static void beforeAll() {
        bootstrapMoneyTransferApplication();
    }

    @Test
    void initialBalancesAreOk() throws IOException {
        assertResponse(HttpStatus.OK_200, "350.00", sendRequest("/accounts/1/balance", GET));
        assertResponse(HttpStatus.OK_200, "250.00", sendRequest("/accounts/2/balance", GET));
        assertResponse(HttpStatus.OK_200, "500.00", sendRequest("/accounts/3/balance", GET));
        assertResponse(HttpStatus.OK_200, "1000.00", sendRequest("/accounts/4/balance", GET));
    }

    @Test
    void initialTransfersAreOk() throws IOException {
        assertGetTransfersResponse(
                HttpStatus.OK_200,
                List.of(
                        new TransferDto(1L, 4L, new BigDecimal("20.00"))
                ), sendRequest("/accounts/1/transfers", GET));

        assertGetTransfersResponse(
                HttpStatus.OK_200,
                new ArrayList<>(),
                sendRequest("/accounts/2/transfers", GET));

        assertGetTransfersResponse(
                HttpStatus.OK_200,
                List.of(
                        new TransferDto(3L, 4L, new BigDecimal("30.00"))
                ), sendRequest("/accounts/3/transfers", GET));

        assertGetTransfersResponse(
                HttpStatus.OK_200,
                List.of(
                        new TransferDto(1L, 4L, new BigDecimal("20.00")),
                        new TransferDto(3L, 4L, new BigDecimal("30.00"))
                ), sendRequest("/accounts/4/transfers", GET));
    }

    @AfterAll
    static void afterAll() {
        stopMoneyTransferApplication();
    }
}
