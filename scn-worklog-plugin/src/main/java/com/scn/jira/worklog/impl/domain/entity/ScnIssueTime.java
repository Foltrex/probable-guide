package com.scn.jira.worklog.impl.domain.entity;

import net.java.ao.RawEntity;
import net.java.ao.schema.PrimaryKey;

public interface ScnIssueTime extends RawEntity<Long> {

    String ID = "ISSUE_ID";

    @PrimaryKey(ID)
    Long getIssueId();

    Long getOriginalEstimate();

    Long getEstimate();

    Long getTimeSpent();
}
