package com.scn.jira.automation.impl.domain.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class Validator {
    @XmlTransient
    private boolean valid = true;
    @XmlElement
    private final List<String> errorMessages = new ArrayList<>();
    @XmlElement
    private final Map<String, String> errors = new HashMap<>();

    public Validator() {
    }

    public Validator(String errorMessage){
        this.errorMessages.add(errorMessage);
    }

    public boolean isValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrorMessages() {
        return this.errorMessages;
    }

    public Map<String, String> getErrors() {
        return this.errors;
    }
}
