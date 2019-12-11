package money_transfers.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import money_transfers.dto.TransferDto;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;

import static money_transfers.tables.Account.ACCOUNT;
import static money_transfers.tables.Transfer.TRANSFER;

@Singleton
public class AccountRepository {

    private final DSLContext dslContext;

    @Inject
    public AccountRepository(DataSource dataSource, SQLDialect sqlDialect) {
        dslContext = DSL.using(dataSource, sqlDialect);
    }

    public void executeMoneyTransfer(TransferDto transferDto) {
        dslContext.transaction((config) -> {
            BigDecimal currentSenderBalance = getBalanceByAccountId(transferDto.getFromAccountId());
            BigDecimal currentRecipientBalance = getBalanceByAccountId(transferDto.getToAccountId());

            updateBalance(transferDto.getFromAccountId(), currentSenderBalance.subtract(transferDto.getAmount()), config);
            updateBalance(transferDto.getToAccountId(), currentRecipientBalance.add(transferDto.getAmount()), config);

            DSL.using(config).insertInto(TRANSFER)
                    .columns(TRANSFER.FROM_ACCOUNT_ID, TRANSFER.TO_ACCOUNT_ID, TRANSFER.AMOUNT)
                    .values(transferDto.getFromAccountId(), transferDto.getToAccountId(), transferDto.getAmount())
                    .execute();
        });
    }

    private void updateBalance(Long accountId, BigDecimal newBalance, Configuration config) {
        DSL.using(config).update(ACCOUNT)
                .set(ACCOUNT.BALANCE, newBalance)
                .where(ACCOUNT.ID.eq(accountId)).execute();
    }

    public BigDecimal getBalanceByAccountId(Long accountId) {
        return dslContext.select(ACCOUNT.BALANCE)
                .from(ACCOUNT)
                .where(ACCOUNT.ID.eq(accountId))
                .fetchOne()
                .into(BigDecimal.class);
    }

    public List<TransferDto> getTransfersByAccountId(Long accountId) {
        return dslContext.select(TRANSFER.FROM_ACCOUNT_ID, TRANSFER.TO_ACCOUNT_ID, TRANSFER.AMOUNT)
                .from(ACCOUNT)
                .join(TRANSFER)
                .on(ACCOUNT.ID.eq(TRANSFER.FROM_ACCOUNT_ID).or(ACCOUNT.ID.eq(TRANSFER.TO_ACCOUNT_ID)))
                .where(ACCOUNT.ID.eq(accountId))
                .fetch(record -> {
                    TransferDto transferDto = new TransferDto();
                    transferDto.setFromAccountId(record.component1());
                    transferDto.setToAccountId(record.component2());
                    transferDto.setAmount(record.component3());
                    return transferDto;
                });
    }
}
