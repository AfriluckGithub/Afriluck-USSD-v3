package org.gh.afriluck.afriluckussd.dto;

import java.util.UUID;

public class SessionDto {

    public UUID id;

    public String msisdn;

    public String sequenceID;

    public String network;

    public String data;

    public Integer position = 0;

    public Integer gameType = 0;

    public String selectedNumbers;

    public Double amount;

    public String timeStamp;

    public String hour;
    public String event;
}
