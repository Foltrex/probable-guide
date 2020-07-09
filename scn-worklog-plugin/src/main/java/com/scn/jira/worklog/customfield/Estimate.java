package com.scn.jira.worklog.customfield;

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
}
