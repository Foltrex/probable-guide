package com.scn.jira.worklog.impl.domain.entity;

import net.java.ao.RawEntity;
import net.java.ao.schema.PrimaryKey;

public interface WorklogWLType extends RawEntity<Long> {

    String ID = "WORKLOG_ID";

    @PrimaryKey(ID)
    Long getWorklogId();

    WLType getWorklogType();
}
