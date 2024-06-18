package org.gh.afriluck.afriluckussd.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.java.Log;

import java.time.LocalDateTime;

@Entity
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @JsonProperty("GameId")
    private String gameId;
    @JsonProperty("GameName")
    private String gameName;
    @JsonProperty("GameDraw")
    private String gameDraw;
    @JsonProperty("Amount")
    private Integer amount;
    @JsonProperty("GameTypeId")
    private Integer gameTypeId;
    @JsonProperty("DrawTime")
    private String drawTime;
    @JsonProperty("StartTime")
    private String startTime;
    @JsonProperty("EndTime")
    private String endTime;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Game(){}

    public Game(Integer id, String gameId, String gameName, String gameDraw, Integer amount, Integer gameTypeId, String drawTime, String startTime, String endTime, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.gameId = gameId;
        this.gameName = gameName;
        this.gameDraw = gameDraw;
        this.amount = amount;
        this.gameTypeId = gameTypeId;
        this.drawTime = drawTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameDraw() {
        return gameDraw;
    }

    public void setGameDraw(String gameDraw) {
        this.gameDraw = gameDraw;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(Integer gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public String getDrawTime() {
        return drawTime;
    }

    public void setDrawTime(String drawTime) {
        this.drawTime = drawTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", gameId='" + gameId + '\'' +
                ", gameName='" + gameName + '\'' +
                ", gameDraw='" + gameDraw + '\'' +
                ", amount=" + amount +
                ", gameTypeId=" + gameTypeId +
                ", drawTime='" + drawTime + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}
