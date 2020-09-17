package com.scn.jira.worklog.customfield;

import java.util.Objects;

public class Estimate {
    private Long original;
    private Long remaining;
    private String formattedOriginal;
    private String formattedRemaining;

    public Estimate() {
    }

    public Estimate(Long original, Long remaining) {
        this.original = original;
        this.remaining = remaining;
    }

    public Estimate(Long original, Long remaining, String formattedOriginal, String formattedRemaining) {
        this.original = original;
        this.remaining = remaining;
        this.formattedOriginal = formattedOriginal;
        this.formattedRemaining = formattedRemaining;
    }

    public Long getOriginal() {
        return original;
    }

    public void setOriginal(Long original) {
        this.original = original;
    }

    public Long getRemaining() {
        return remaining;
    }

    public void setRemaining(Long remaining) {
        this.remaining = remaining;
    }

    public String getFormattedOriginal() {
        return formattedOriginal;
    }

    public void setFormattedOriginal(String formattedOriginal) {
        this.formattedOriginal = formattedOriginal;
    }

    public String getFormattedRemaining() {
        return formattedRemaining;
    }

    public void setFormattedRemaining(String formattedRemaining) {
        this.formattedRemaining = formattedRemaining;
    }

    public boolean isEmpty() {
        return original == null && remaining == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof Estimate) {
            Estimate estimateObj = (Estimate) obj;
            return Objects.equals(this.getOriginal(), estimateObj.getOriginal())
                && Objects.equals(this.getRemaining(), estimateObj.getRemaining());
        }

        return false;
    }
}
