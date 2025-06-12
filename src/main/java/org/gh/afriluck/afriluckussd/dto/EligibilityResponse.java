package org.gh.afriluck.afriluckussd.dto;

public class EligibilityResponse {
    private String message;
    private boolean can_participate;

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCan_participate() {
        return can_participate;
    }

    public void setCan_participate(boolean can_participate) {
        this.can_participate = can_participate;
    }
}

