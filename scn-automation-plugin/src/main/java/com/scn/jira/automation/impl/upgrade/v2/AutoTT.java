package com.scn.jira.automation.impl.upgrade.v2;

import net.java.ao.Preload;

import java.sql.Timestamp;

@Preload
public interface AutoTT extends com.scn.jira.automation.impl.domain.entity.AutoTT {
    Timestamp getStartDate();

    void setStartDate(Timestamp startDate);
}
