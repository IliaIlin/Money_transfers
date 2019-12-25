package moneytransfers.service;

import moneytransfers.dto.TransferDto;
import moneytransfers.exception.TransferExecutionException;
import moneytransfers.repository.AccountRepository;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    private static final Long FIRST_ACCOUNT_ID = 1L;
    private static final Long SECOND_ACCOUNT_ID = 2L;
    private static final BigDecimal LOW_AMOUNT = BigDecimal.valueOf(50);
    private static final BigDecimal AVERAGE_AMOUNT = BigDecimal.valueOf(100);
    private static final BigDecimal BIG_AMOUNT = BigDecimal.valueOf(200);

    @BeforeEach
    void setUp() {
        DSLContext dslContext = DSL.using(new MockConnection(c -> new MockResult[]{}), SQLDialect.H2);
        accountService = new AccountService(accountRepository, dslContext);
    }

    @Test
    void executeMoneyTransfer_currentSenderBalanceIsNull_shouldThrowException() {
        assertThrows(TransferExecutionException.class, () -> accountService.executeMoneyTransfer(
                new TransferDto(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, AVERAGE_AMOUNT)));
    }

    @Test
    void executeMoneyTransfer_currentSenderBalanceIsSmallerThanAmount_shouldThrowException() {
        when(accountRepository.getBalanceByAccountId(eq(FIRST_ACCOUNT_ID), any()))
                .thenReturn(LOW_AMOUNT);

        assertThrows(TransferExecutionException.class, () -> accountService.executeMoneyTransfer(
                new TransferDto(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, AVERAGE_AMOUNT)));
    }

    @Test
    void executeMoneyTransfer_currentRecipientBalanceIsNull_shouldThrowException() {
        when(accountRepository.getBalanceByAccountId(eq(FIRST_ACCOUNT_ID), any()))
                .thenReturn(BIG_AMOUNT);

        assertThrows(TransferExecutionException.class, () -> accountService.executeMoneyTransfer(
                new TransferDto(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, AVERAGE_AMOUNT)));
    }

    @Test
    void executeMoneyTransfer_happyFlow_transferShouldBeExecuted() {
        when(accountRepository.getBalanceByAccountId(eq(FIRST_ACCOUNT_ID), any()))
                .thenReturn(BIG_AMOUNT);
        when(accountRepository.getBalanceByAccountId(eq(SECOND_ACCOUNT_ID), any()))
                .thenReturn(LOW_AMOUNT);

        final TransferDto transferDto = new TransferDto(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, AVERAGE_AMOUNT);
        accountService.executeMoneyTransfer(
                transferDto);

        verify(accountRepository).updateBalance(eq(FIRST_ACCOUNT_ID),
                eq(BIG_AMOUNT.subtract(AVERAGE_AMOUNT)), any());
        verify(accountRepository).updateBalance(eq(SECOND_ACCOUNT_ID),
                eq(LOW_AMOUNT.add(AVERAGE_AMOUNT)), any());
        verify(accountRepository).insertTransferData(eq(transferDto), any());
    }
}
