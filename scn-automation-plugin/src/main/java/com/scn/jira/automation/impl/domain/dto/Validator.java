package com.scn.jira.automation.impl.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@JsonAutoDetect
public class Validator {
    @JsonIgnore
    private boolean valid = true;
    private final List<String> errorMessages = new ArrayList<>();
    private final Map<String, String> errors = new HashMap<>();

    public Validator(String errorMessage) {
        this.errorMessages.add(errorMessage);
    }
}
