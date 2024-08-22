package org.gh.afriluck.afriluckussd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerDepositResponseDto {

    @JsonProperty("success")
    public String success;
}
