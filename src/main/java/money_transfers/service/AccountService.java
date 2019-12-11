package money_transfers.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import money_transfers.MoneyTransferApplication;
import money_transfers.dto.TransferDto;
import money_transfers.exception.TransferExecutionException;
import money_transfers.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

@Singleton
public class AccountService {

    private final AccountRepository accountRepository;
    private static final Logger LOG = LoggerFactory.getLogger(MoneyTransferApplication.class);

    @Inject
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void executeMoneyTransfer(TransferDto transferDto) {
        LOG.debug("Transfer to be processed: " + transferDto);
        try {
            BigDecimal balanceOnSenderAccount = accountRepository.getBalanceByAccountId(transferDto.getFromAccountId());
            if (balanceOnSenderAccount.compareTo(transferDto.getAmount()) < 0) {
                throw new TransferExecutionException();
            }
            accountRepository.executeMoneyTransfer(transferDto);
        } catch (Exception e) {
            throw new TransferExecutionException();
        }
    }

    public BigDecimal getBalanceByAccountId(Long accountId) {
        return accountRepository.getBalanceByAccountId(accountId);
    }

    public List<TransferDto> getTransfersByAccountId(Long accountId) {
        return accountRepository.getTransfersByAccountId(accountId);
    }
}
