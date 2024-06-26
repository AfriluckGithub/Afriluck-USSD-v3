package org.gh.afriluck.afriluckussd.dto;

public class DiscountResponse {

    private Double amount;
    private Boolean isValid;

    public DiscountResponse(Double amount, Boolean isValid) {
        this.amount = amount;
        this.isValid = isValid;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    @Override
    public String toString() {
        return "DiscountResponse{" +
                "amount=" + amount +
                ", isValid=" + isValid +
                '}';
    }
}
