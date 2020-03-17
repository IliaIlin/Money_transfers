package moneytransfers.service;

import com.google.inject.Guice;
import moneytransfers.MoneyTransferModule;
import moneytransfers.dto.TransferDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static moneytransfers.TestUtils.assertEqualsTransferList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountServiceIntegrationTest {
    private static AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = Guice.createInjector(new MoneyTransferModule())
                .getInstance(AccountService.class);
    }

    @Test
    void getBalanceByAccountId_existingInitialAccounts_shouldReturnInitialBalances() {
        assertEquals(new BigDecimal("350.00"), accountService.getBalanceByAccountId(1L));
        assertEquals(new BigDecimal("250.00"), accountService.getBalanceByAccountId(2L));
        assertEquals(new BigDecimal("500.00"), accountService.getBalanceByAccountId(3L));
        assertEquals(new BigDecimal("1000.00"), accountService.getBalanceByAccountId(4L));
    }

    @Test
    void getTransfersByAccountId_existingInitialAccounts_shouldReturnInitialTransfers() {
        assertEqualsTransferList(
                List.of(
                        new TransferDto(1L, 4L, new BigDecimal("20.00"))
                ),
                accountService.getTransfersByAccountId(1L));

        assertEqualsTransferList(
                new ArrayList<>(),
                accountService.getTransfersByAccountId(2L));

        assertEqualsTransferList(
                List.of(
                        new TransferDto(3L, 4L, new BigDecimal("30.00"))
                ),
                accountService.getTransfersByAccountId(3L));

        assertEqualsTransferList(
                List.of(
                        new TransferDto(1L, 4L, new BigDecimal("20.00")),
                        new TransferDto(3L, 4L, new BigDecimal("30.00"))
                ),
                accountService.getTransfersByAccountId(4L));
    }

    @Test
    void getTransfersByAccountId_notExistingAccount_shouldReturnEmptyList() {
        assertEqualsTransferList(new ArrayList<>(), accountService.getTransfersByAccountId(5L));
    }

    @Test
    void executeMoneyTransfer_happyFlow_balancesAndTransferListAreUpdatedCorrespondingly() {
        final TransferDto transferDto = new TransferDto(1L, 3L, new BigDecimal("20.00"));
        accountService.executeMoneyTransfer(transferDto);

        assertEquals(new BigDecimal("330.00"), accountService.getBalanceByAccountId(1L));
        assertEquals(new BigDecimal("520.00"), accountService.getBalanceByAccountId(3L));

        assertTrue(accountService.getTransfersByAccountId(1L).contains(transferDto));
        assertTrue(accountService.getTransfersByAccountId(3L).contains(transferDto));
    }
}
