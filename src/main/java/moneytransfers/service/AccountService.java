package moneytransfers.service;

import moneytransfers.MoneyTransferApplication;
import moneytransfers.dto.TransferDto;
import moneytransfers.exception.NoSuchAccountException;
import moneytransfers.exception.TransferExecutionException;
import moneytransfers.repository.AccountRepository;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
        if (transferDto.getAmount().signum() < 0){
            throw new TransferExecutionException();
        }
        try {
            dslContext.transaction((config) -> {
                Optional<BigDecimal> currentSenderBalance = accountRepository.getBalanceByAccountId(transferDto.getFromAccountId(), config);
                currentSenderBalance.orElseThrow(NoSuchAccountException::new);
                if (currentSenderBalance.get().compareTo(transferDto.getAmount()) < 0) {
                    throw new TransferExecutionException();
                }
                Optional<BigDecimal> currentRecipientBalance = accountRepository.getBalanceByAccountId(transferDto.getToAccountId(), config);
                currentRecipientBalance.orElseThrow(NoSuchAccountException::new);
                accountRepository.updateBalance(transferDto.getFromAccountId(),
                        currentSenderBalance.get().subtract(transferDto.getAmount()), config);
                accountRepository.updateBalance(transferDto.getToAccountId(),
                        currentRecipientBalance.get().add(transferDto.getAmount()), config);
                accountRepository.insertTransferData(transferDto, config);
            });
        } catch (NoSuchAccountException e) {
            throw e;
        } catch (Exception e) {
            throw new TransferExecutionException();
        }
    }

    public BigDecimal getBalanceByAccountId(Long accountId) {
        Optional<BigDecimal> balanceByAccountId = accountRepository.getBalanceByAccountId(accountId, dslContext.configuration());
        balanceByAccountId.orElseThrow(NoSuchAccountException::new);
        return balanceByAccountId.get();
    }

    public List<TransferDto> getTransfersByAccountId(Long accountId) {
        return accountRepository.getTransfersByAccountId(accountId, dslContext.configuration());
    }
}
