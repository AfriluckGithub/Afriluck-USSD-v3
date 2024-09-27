package org.gh.afriluck.afriluckussd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Event {

    public String event;
    public String hour;


    public Event() {
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    @Override
    public String toString() {
        return "Event{" +
                "event='" + event + '\'' +
                ", hour='" + hour + '\'' +
                '}';
    }
}
