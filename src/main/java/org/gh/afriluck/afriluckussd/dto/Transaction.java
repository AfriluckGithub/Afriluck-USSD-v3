package org.gh.afriluck.afriluckussd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Transaction {

    @JsonProperty("msisdn")
    private String msisdn;
    @JsonProperty("total_amount")
    private Double totalAmount;
    @JsonProperty("game_id")
    private String gameId;
    @JsonProperty("draw_code")
    private String drawCode;
    @JsonProperty("entry_amount")
    private Double entryAmount;
    @JsonProperty("bet_type_code")
    private Integer betTypeCode;
    @JsonProperty("bet_type")
    private String betType;
    @JsonProperty("selected_numbers")
    private String selectedNumbers;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("discounted_amount")
    private Double discountedAmount;

    public Transaction() {
    }

    public Transaction(String msisdn, Double totalAmount, String gameId, String drawCode, Double entryAmount, Integer betTypeCode, String betType, String selectedNumbers, String channel, Double discountedAmount) {
        this.msisdn = msisdn;
        this.totalAmount = totalAmount;
        this.gameId = gameId;
        this.drawCode = drawCode;
        this.entryAmount = entryAmount;
        this.betTypeCode = betTypeCode;
        this.betType = betType;
        this.selectedNumbers = selectedNumbers;
        this.channel = channel;
        this.discountedAmount = discountedAmount;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getDrawCode() {
        return drawCode;
    }

    public void setDrawCode(String drawCode) {
        this.drawCode = drawCode;
    }

    public Double getEntryAmount() {
        return entryAmount;
    }

    public void setEntryAmount(Double entryAmount) {
        this.entryAmount = entryAmount;
    }

    public Integer getBetTypeCode() {
        return betTypeCode;
    }

    public void setBetTypeCode(Integer betTypeCode) {
        this.betTypeCode = betTypeCode;
    }

    public String getBetType() {
        return betType;
    }

    public void setBetType(String betType) {
        this.betType = betType;
    }

    public String getSelectedNumbers() {
        return selectedNumbers;
    }

    public void setSelectedNumbers(String selectedNumbers) {
        this.selectedNumbers = selectedNumbers;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Double getDiscountedAmount() {
        return discountedAmount;
    }

    public void setDiscountedAmount(Double discountedAmount) {
        this.discountedAmount = discountedAmount;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "msisdn='" + msisdn + '\'' +
                ", totalAmount=" + totalAmount +
                ", gameId='" + gameId + '\'' +
                ", drawCode='" + drawCode + '\'' +
                ", entryAmount=" + entryAmount +
                ", betTypeCode=" + betTypeCode +
                ", betType='" + betType + '\'' +
                ", selectedNumbers='" + selectedNumbers + '\'' +
                ", channel='" + channel + '\'' +
                ", discountedAmount='" + discountedAmount + '\'' +
                '}';
    }
}
