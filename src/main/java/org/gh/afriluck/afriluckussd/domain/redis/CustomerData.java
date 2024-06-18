package org.gh.afriluck.afriluckussd.domain.redis;

import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("CustomerData")
public class CustomerData implements Serializable {
    private String id;
    public String sequenceId;
    public String data;
    public String selectedNumbers;
    public int gameType;
    public int menu;

    public CustomerData(String id, String sequenceId, String data, String selectedNumbers, int gameType, int menu) {
        this.id = id;
        this.data = data;
        this.sequenceId = sequenceId;
        this.selectedNumbers = selectedNumbers;
        this.gameType = gameType;
        this.menu = menu;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSelectedNumbers() {
        return selectedNumbers;
    }

    public void setSelectedNumbers(String selectedNumbers) {
        this.selectedNumbers = selectedNumbers;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public int getMenu() {
        return menu;
    }

    public void setMenu(int menu) {
        this.menu = menu;
    }
}
