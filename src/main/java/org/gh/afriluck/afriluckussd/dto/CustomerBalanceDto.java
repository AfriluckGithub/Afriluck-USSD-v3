package org.gh.afriluck.afriluckussd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerBalanceDto {

    @JsonProperty("balance")
    public String balance;
    @JsonProperty("bonus")
    public String bonus;
}
