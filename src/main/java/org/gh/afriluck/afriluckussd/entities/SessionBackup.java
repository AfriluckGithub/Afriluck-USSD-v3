package org.gh.afriluck.afriluckussd.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "session_backup")
public class SessionBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;
    public String msisdn;
    public String network;
    private Double amount;
    private String timeStamp;
    private Integer gameType;


    public SessionBackup() {
    }

    public SessionBackup(String msisdn, String network, Double amount, String timeStamp, Integer gameType) {
        this.msisdn = msisdn;
        this.network = network;
        this.amount = amount;
        this.timeStamp = timeStamp;
        this.gameType = gameType;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getGameType() {
        return gameType;
    }

    public void setGameType(Integer gameType) {
        this.gameType = gameType;
    }
}
