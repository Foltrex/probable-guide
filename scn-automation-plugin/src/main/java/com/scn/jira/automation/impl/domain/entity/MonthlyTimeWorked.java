package com.scn.jira.automation.impl.domain.entity;

import net.java.ao.schema.NotNull;

import java.util.Date;

public interface MonthlyTimeWorked extends WithId {
    @NotNull
    String getUserKey();

    void setUserKey(String userKey);

    @NotNull
    Long getProjectId();

    void setProjectId(Long projectId);

    Date getMonth();

    void setMonth(Date month);

    Long getTime();

    void setTime(Long time);
}
