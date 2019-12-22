package money_transfers.repository;

import com.google.inject.Singleton;
import money_transfers.dto.TransferDto;
import org.jooq.Configuration;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.List;

import static money_transfers.tables.Account.ACCOUNT;
import static money_transfers.tables.Transfer.TRANSFER;

@Singleton
public class AccountRepository {

    public void insertTransferData(TransferDto transferDto, Configuration config) {
        DSL.using(config).insertInto(TRANSFER)
                .columns(TRANSFER.FROM_ACCOUNT_ID, TRANSFER.TO_ACCOUNT_ID, TRANSFER.AMOUNT)
                .values(transferDto.getFromAccountId(), transferDto.getToAccountId(), transferDto.getAmount())
                .execute();
    }

    public void updateBalance(Long accountId, BigDecimal newBalance, Configuration config) {
        DSL.using(config).update(ACCOUNT)
                .set(ACCOUNT.BALANCE, newBalance)
                .where(ACCOUNT.ID.eq(accountId)).execute();
    }

    public BigDecimal getBalanceByAccountId(Long accountId, Configuration config) {
        return DSL.using(config).select(ACCOUNT.BALANCE)
                .from(ACCOUNT)
                .where(ACCOUNT.ID.eq(accountId))
                .fetchOne()
                .into(BigDecimal.class);
    }

    public List<TransferDto> getTransfersByAccountId(Long accountId, Configuration config) {
        return DSL.using(config).select(TRANSFER.FROM_ACCOUNT_ID, TRANSFER.TO_ACCOUNT_ID, TRANSFER.AMOUNT)
                .from(ACCOUNT)
                .join(TRANSFER)
                .on(ACCOUNT.ID.eq(TRANSFER.FROM_ACCOUNT_ID).or(ACCOUNT.ID.eq(TRANSFER.TO_ACCOUNT_ID)))
                .where(ACCOUNT.ID.eq(accountId))
                .fetch(record -> new TransferDto(
                        record.component1(), record.component2(), record.component3()));
    }
}
