package com.scn.jira.automation.impl.domain.entity;

import net.java.ao.schema.NotNull;

import java.util.Date;

public interface UsersAbsence extends WithId {
    @NotNull
    String getUserKey();

    void setUserKey(String userKey);

    Date getDateFrom();

    void setDateFrom(Date dateFrom);

    Date getDateTo();

    void setDateTo(Date dateTo);
}
