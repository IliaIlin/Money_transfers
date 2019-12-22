package moneytransfers.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Objects;

public class TransferDto {
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;

    public TransferDto() {
    }

    public TransferDto(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @JsonIgnore
    public boolean isValid() {
        return fromAccountId != null && toAccountId != null && amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferDto that = (TransferDto) o;
        return fromAccountId.equals(that.fromAccountId) &&
                toAccountId.equals(that.toAccountId) &&
                amount.equals(that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromAccountId, toAccountId, amount);
    }

    @Override
    public String toString() {
        return "TransferDto{" +
                "fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", amount=" + amount +
                '}';
    }
}
