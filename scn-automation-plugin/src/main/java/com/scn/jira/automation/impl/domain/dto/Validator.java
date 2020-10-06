package com.scn.jira.automation.impl.domain.dto;

import java.util.ArrayList;
import java.util.List;

public class Validator {
    private boolean valid = true;
    private final List<String> errorMessages = new ArrayList<>();

    public boolean isValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrorMessages() {
        return this.errorMessages;
    }
}
