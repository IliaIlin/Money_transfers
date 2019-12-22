package money_transfers;

import money_transfers.dto.TransferDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static money_transfers.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InitialDataTest {

    @BeforeAll
    static void beforeAll() {
        bootstrapMoneyTransferApplication(new MoneyTransferModule());
    }

    @Test
    void initialBalancesAreOk() throws IOException {
        assertEquals("350.00", getResponse("/accounts/1/balance", GET));
        assertEquals("250.00", getResponse("/accounts/2/balance", GET));
        assertEquals("500.00", getResponse("/accounts/3/balance", GET));
        assertEquals("1000.00", getResponse("/accounts/4/balance", GET));
    }

    @Test
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

    @AfterAll
    static void afterAll() {
        stopMoneyTransferApplication();
    }
}
