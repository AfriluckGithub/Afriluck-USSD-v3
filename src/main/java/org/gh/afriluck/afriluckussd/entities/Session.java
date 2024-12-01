package org.gh.afriluck.afriluckussd.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session")
public class Session {

    public String msisdn;
    public String network;
    public String message;
    @Id
    @UuidGenerator
    private UUID id;
    private String sequenceID;
    public String data;

    private Integer position;

    private Integer gameType;

    private String selectedNumbers;

    private Double amount;

    private String timeStamp;

    private String gameTypeId;

    private String gameId;

    private String betTypeCode;

    private Integer gameTypeCode;

    private String currentGame;

    private LocalDateTime createdDate;

    private Integer max;
    private Integer min;
    private String callBackMessage;
    private Boolean couponApplied;
    private Double discountedAmount;
    @Column(name = "passed_welcome_msg")
    private boolean passedWelcomeMessage;
    @Column(name = "secondStep")
    private boolean secondStep;
    @Column(name = "start")
    private boolean start;
    @Column(name = "menu_choice")
    private int menuChoice;
    @Column(name = "next_step")
    private Integer nextStep=0;
    @Column(name = "is_morning")
    private boolean isMorning;
    @Column(name = "is_afternoon", nullable = false)
    private boolean isAfternoon;
    @Column(name = "is_evening")
    private boolean isEvening;
    @Column(name = "reset")
    private boolean reset;
    @Column(name = "back_pressed")
    private boolean isBackPressed;

    private String hour;
    private String event;


    private LocalDateTime updatedDate;

    public Session() {
    }


    public Session(UUID id, String message, String sequenceID, String network, String msisdn, String data, Integer position, Integer gameType, String selectedNumbers, Double amount, String gameTypeId, String timeStamp, String gameId, String betTypeCode, Integer gameTypeCode, String currentGame, Integer max, Integer min, String callBackMessage, Boolean couponApplied, Double discountedAmount, Boolean passedWelcomeMessage, Boolean start, Boolean secondStep, Integer menuChoice, Integer nextStep, Boolean isMorning, Boolean isAfternoon, Boolean isEvening, Boolean reset, Boolean isBackPressed) {
        this.id = id;
        this.sequenceID = sequenceID;
        this.network = network;
        this.msisdn = msisdn;
        this.data = data;
        this.position = position;
        this.gameType = gameType;
        this.selectedNumbers = selectedNumbers;
        this.amount = amount;
        this.gameTypeId = gameTypeId;
        this.timeStamp = timeStamp;
        this.gameId = gameId;
        this.betTypeCode = betTypeCode;
        this.gameTypeCode = gameTypeCode;
        this.currentGame = currentGame;
        this.message = message;
        this.max = max;
        this.min = min;
        this.callBackMessage = callBackMessage;
        this.couponApplied = couponApplied;
        this.discountedAmount = discountedAmount;
        this.passedWelcomeMessage = passedWelcomeMessage;
        this.start = start;
        this.secondStep = secondStep;
        this.menuChoice = menuChoice;
        this.nextStep = nextStep;
        this.isMorning = isMorning;
        this.isAfternoon = isAfternoon;
        this.reset = reset;
        this.isBackPressed = isBackPressed;
        this.isEvening = isEvening;
    }

    @PrePersist
    protected void onCreate() {
        position = 0;
        gameType = 0;
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public void setMenuChoice(int menuChoice) {
        this.menuChoice = menuChoice;
    }

    public int getMenuChoice() {
        return menuChoice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSequenceID() {
        return sequenceID;
    }

    public void setSequenceId(String sequenceID) {
        this.sequenceID = sequenceID;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getGameType() {
        return gameType;
    }

    public void setGameType(Integer gameType) {
        this.gameType = gameType;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getSelectedNumbers() {
        return selectedNumbers;
    }

    public void setSelectedNumbers(String selectedNumbers) {
        this.selectedNumbers = selectedNumbers;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(String gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getBetTypeCode() {
        return betTypeCode;
    }

    public void setBetTypeCode(String betTypeCode) {
        this.betTypeCode = betTypeCode;
    }

    public Integer getGameTypeCode() {
        return gameTypeCode;
    }

    public void setGameTypeCode(Integer gameTypeCode) {
        this.gameTypeCode = gameTypeCode;
    }

    public String getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame = currentGame;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public String getCallBackMessage() {
        return callBackMessage;
    }

    public void setCallBackMessage(String callBackMessage) {
        this.callBackMessage = callBackMessage;
    }

    public Boolean getCouponApplied() {
        return couponApplied;
    }

    public void setCouponApplied(Boolean couponApplied) {
        this.couponApplied = couponApplied;
    }

    public Double getDiscountedAmount() {
        return discountedAmount;
    }

    public void setDiscountedAmount(Double discountedAmount) {
        this.discountedAmount = discountedAmount;
    }

    public void setPassedWelcomeMessage(boolean passedWelcomeMessage) {
        this.passedWelcomeMessage = passedWelcomeMessage;
    }

    public boolean isPassedWelcomeMessage() {
        return passedWelcomeMessage;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public void setNextStep(Integer nextStep) {
        this.nextStep = nextStep;
    }

    public Integer getNextStep() {
        return nextStep;
    }

    public boolean isSecondStep() {
        return secondStep;
    }

    public void setSecondStep(boolean secondStep) {
        this.secondStep = secondStep;
    }

    public boolean isMorning() {
        return isMorning;
    }

    public void setMorning(boolean morning) {
        isMorning = morning;
    }

    public boolean isAfternoon() {
        return isAfternoon;
    }

    public void setAfternoon(boolean afternoon) {
        isAfternoon = afternoon;
    }

    public boolean isEvening() {
        return isEvening;
    }

    public void setEvening(boolean evening) {
        isEvening = evening;
    }

    public boolean isReset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }

    public void setBackPressed(boolean backPressed) {
        isBackPressed = backPressed;
    }

    public boolean isBackPressed() {
        return isBackPressed;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", msisdn='" + msisdn + '\'' +
                ", sequenceID='" + sequenceID + '\'' +
                ", network='" + network + '\'' +
                ", data='" + data + '\'' +
                ", position=" + position +
                ", amount=" + amount +
                ", gameType=" + gameType +
                ", selectedNumbers='" + selectedNumbers + '\'' +
                ", callBackMessage='" + callBackMessage + '\'' +
                ", couponApplied='" + couponApplied + '\'' +
                ", discountedAmount='" + discountedAmount + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", passedWelcomeMessage'"+ passedWelcomeMessage+'\''+
                ", start'"+ start+'\''+
                ", secondStep'"+ secondStep+'\''+
                ", menuChoice'"+ menuChoice+'\''+
                ", nextStep'"+ nextStep+'\''+
                ", isMorning'"+ isMorning+'\''+
                ", isAfternoon'"+ isAfternoon+'\''+
                ", reset'"+ reset+'\''+
                ", isBackPressed'"+ isBackPressed+'\''+
                '}';
    }
}
