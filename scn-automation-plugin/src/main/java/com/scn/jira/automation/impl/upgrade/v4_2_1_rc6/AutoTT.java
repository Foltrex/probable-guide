package com.scn.jira.automation.impl.upgrade.v4_2_1_rc6;

import net.java.ao.Preload;

import java.sql.Timestamp;

@Preload
public interface AutoTT extends com.scn.jira.automation.impl.domain.entity.AutoTT {
    String getUsername();

    void setUsername(String username);

    String getWorklogTypeId();

    void setWorklogTypeId(String worklogTypeId);

    Timestamp getStartDate();

    void setStartDate(Timestamp startDate);
}
