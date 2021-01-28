package com.scn.jira.automation.impl.domain.entity;

import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

import java.sql.Timestamp;

public interface Worklog extends Updatable {
    @NotNull
    Long getProjectId();

    void setProjectId(Long projectId);

    @NotNull
    Long getIssueId();

    void setIssueId(Long issueId);

    @StringLength(-1)
    String getWorklogBody();

    void setWorklogBody(String worklogBody);

    Timestamp getStartDate();

    void setStartDate(Timestamp startDate);

    Long getTimeWorked();

    void setTimeWorked(Long timeWorked);

    String getWorklogTypeId();

    void setWorklogTypeId(String worklogTypeId);
}
