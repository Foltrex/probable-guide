package com.scn.jira.automation.impl.domain.dto;

import javax.annotation.Nonnull;

public enum WorklogKind {
    WORKLOG_SCN("WL*"),
    WORKLOG("WL");

    private final String value;

    WorklogKind(String value) {
        this.value = value;
    }

    @Nonnull
    public static WorklogKind getByValue(String value) {
        for (WorklogKind kind : values()) {
            if (kind.value.equals(value)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("There is no such value for WorklogKind: " + value);
    }

    @Override
    public String toString() {
        return this.value;
    }
}
