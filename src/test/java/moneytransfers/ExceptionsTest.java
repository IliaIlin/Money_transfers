package moneytransfers;

import moneytransfers.dto.TransferDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import static moneytransfers.MoneyTransferApplication.*;
import static moneytransfers.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionsTest {

    @BeforeAll
    static void beforeAll() {
        bootstrapMoneyTransferApplication(new MoneyTransferModule());
    }

    @Test
    void getBalance_notExistingAccountId_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(INTERNAL_SERVER_ERROR_MESSAGE, getResponse("/accounts/100/balance", GET));
    }

    @Test
    void getBalance_notNumericAccountId_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(INTERNAL_SERVER_ERROR_MESSAGE, getResponse("/accounts/str/balance", GET));
    }

    @Test
    void getTransfers_notExistingAccountId_shouldReturnEmptyList() throws IOException {
        assertTransfersResponse(new ArrayList<>(), getResponse("/accounts/15/transfers", GET));
    }

    @Test
    void transfer_notExistingAccountId_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(TRANSFER_EXECUTION_ERROR_MESSAGE,
                getResponse("/transfer", POST,
                        new TransferDto(10L, 4L, new BigDecimal("20.00"))));
    }

    @Test
    void transfer_notSufficientFunds_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(TRANSFER_EXECUTION_ERROR_MESSAGE,
                getResponse("/transfer", POST,
                        new TransferDto(1L, 4L, new BigDecimal("1000.00"))));
    }


    @Test
    void transfer_unspecifiedAmount_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(INVALID_REQUEST_BODY_MESSAGE,
                getResponse("/transfer", POST, new TransferDto(1L, 4L, null)));
    }

    @Test
    void transfer_invalidRequestBody_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(INVALID_REQUEST_BODY_MESSAGE,
                getResponse("/transfer", POST, null));
    }

    @Test
    void transfer_negativeAmountOfMoney_shouldReturnCorrectErrorMessage() throws IOException {
        assertEquals(INVALID_REQUEST_BODY_MESSAGE,
                getResponse("/transfer", POST,
                        new TransferDto(1L, 4L, new BigDecimal("-100.00"))));
    }

    @AfterAll
    static void afterAll() {
        stopMoneyTransferApplication();
    }
}
