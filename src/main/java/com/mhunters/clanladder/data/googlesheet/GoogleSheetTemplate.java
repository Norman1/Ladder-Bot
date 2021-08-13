package com.mhunters.clanladder.data.googlesheet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleSheetTemplate {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Link")
    private String link;
}
