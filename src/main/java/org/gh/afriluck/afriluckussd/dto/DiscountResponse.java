package org.gh.afriluck.afriluckussd.dto;

public class DiscountResponse {

    private Integer amount;
    private Boolean isValid;

    public DiscountResponse(Integer amount, Boolean isValid) {
        this.amount = amount;
        this.isValid = isValid;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
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
