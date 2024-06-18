package org.gh.afriluck.afriluckussd.dto;

public class GameDto {
    private String GameId;
    private String GameName;
    private String GameDraw;
    private Double Amount;
    private Integer GameTypeId;
    private String DrawTime;
    private String StartTime;
    private String EndTime;

    public GameDto() {}

    public GameDto(String gameId, String gameName, String gameDraw, Double amount, Integer gameTypeId, String drawTime, String startTime, String endTime) {
        GameId = gameId;
        GameName = gameName;
        GameDraw = gameDraw;
        Amount = amount;
        GameTypeId = gameTypeId;
        DrawTime = drawTime;
        StartTime = startTime;
        EndTime = endTime;
    }

    public String getGameId() {
        return GameId;
    }

    public void setGameId(String gameId) {
        GameId = gameId;
    }

    public String getGameName() {
        return GameName;
    }

    public void setGameName(String gameName) {
        GameName = gameName;
    }

    public String getGameDraw() {
        return GameDraw;
    }

    public void setGameDraw(String gameDraw) {
        GameDraw = gameDraw;
    }

    public Double getAmount() {
        return Amount;
    }

    public void setAmount(Double amount) {
        Amount = amount;
    }

    public Integer getGameTypeId() {
        return GameTypeId;
    }

    public void setGameTypeId(Integer gameTypeId) {
        GameTypeId = gameTypeId;
    }

    public String getDrawTime() {
        return DrawTime;
    }

    public void setDrawTime(String drawTime) {
        DrawTime = drawTime;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }
}
