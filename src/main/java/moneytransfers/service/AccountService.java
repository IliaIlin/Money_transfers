package moneytransfers.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import moneytransfers.MoneyTransferApplication;
import moneytransfers.dto.TransferDto;
import moneytransfers.exception.TransferExecutionException;
import moneytransfers.repository.AccountRepository;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

@Singleton
public class AccountService {

    private final AccountRepository accountRepository;
    private final DSLContext dslContext;

    private static final Logger LOG = LoggerFactory.getLogger(MoneyTransferApplication.class);

    @Inject
    public AccountService(AccountRepository accountRepository,
                          DSLContext dslContext) {
        this.accountRepository = accountRepository;
        this.dslContext = dslContext;
    }

    public void executeMoneyTransfer(TransferDto transferDto) {
        LOG.debug("Transfer to be processed: " + transferDto);
        try {
            dslContext.transaction((config) -> {
                BigDecimal currentSenderBalance = accountRepository.getBalanceByAccountId(transferDto.getFromAccountId(), config);
                if (currentSenderBalance.compareTo(transferDto.getAmount()) < 0) {
                    throw new TransferExecutionException();
                }
                BigDecimal currentRecipientBalance = accountRepository.getBalanceByAccountId(transferDto.getToAccountId(), config);
                accountRepository.updateBalance(transferDto.getFromAccountId(),
                        currentSenderBalance.subtract(transferDto.getAmount()), config);
                accountRepository.updateBalance(transferDto.getToAccountId(),
                        currentRecipientBalance.add(transferDto.getAmount()), config);
                accountRepository.insertTransferData(transferDto, config);
            });
        } catch (Exception e) {
            throw new TransferExecutionException();
        }
    }

    public BigDecimal getBalanceByAccountId(Long accountId) {
        return accountRepository.getBalanceByAccountId(accountId, dslContext.configuration());
    }

    public List<TransferDto> getTransfersByAccountId(Long accountId) {
        return accountRepository.getTransfersByAccountId(accountId, dslContext.configuration());
    }
}
