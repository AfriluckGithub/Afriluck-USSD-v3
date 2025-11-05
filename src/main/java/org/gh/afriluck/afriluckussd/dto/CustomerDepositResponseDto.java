package org.gh.afriluck.afriluckussd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerDepositResponseDto {

    @JsonProperty("success")
    public String success;

    // No-args constructor
    public CustomerDepositResponseDto() {
    }

    // All-args constructor
    public CustomerDepositResponseDto(String success) {
        this.success = success;
    }


    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }
    
}
