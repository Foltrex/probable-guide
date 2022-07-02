package com.scn.jira.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Getter
@JsonAutoDetect
public class ErrorResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String errorMessage;
    private final Map<String, String> errors = new HashMap<>();

    public ErrorResult(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
