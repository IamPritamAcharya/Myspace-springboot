package com.backend.codechef;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodechefContest {

    @JsonProperty("contest_code")
    private String contestCode;

    @JsonProperty("contest_name")
    private String contestName;

    @JsonProperty("contest_start_date")
    private String startDate;

    @JsonProperty("contest_end_date")
    private String endDate;
}
