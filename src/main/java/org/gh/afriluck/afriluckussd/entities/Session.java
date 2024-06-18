package org.gh.afriluck.afriluckussd.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    public String msisdn;

    private String sequenceID;

    public String network;

    private String data;

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

    private LocalDateTime updatedDate;

    public Session() {};

    public Session(Integer id, String sequenceID, String network, String msisdn, String data, Integer position, Integer gameType, String selectedNumbers, Double amount, String gameTypeId,  String timeStamp, String gameId, String betTypeCode, Integer gameTypeCode, String currentGame) {
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", msisdn='" + msisdn + '\'' +
                ", sequenceID='" + sequenceID + '\'' +
                ", network='" + network + '\'' +
                ", data='" + data + '\'' +
                ", position=" + position +
                ", gameType=" + gameType +
                ", selectedNumbers='" + selectedNumbers + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }
}
