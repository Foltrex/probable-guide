package com.scn.jira.automation.impl.domain.entity;

import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Unique;

@Preload
public interface AutoTT extends Updatable {
    @Unique
    String getUserKey();

    void setUserKey(String userKey);

    @NotNull
    Long getProjectId();

    void setProjectId(Long projectID);

    @NotNull
    Long getIssueId();

    void setIssueId(Long issueID);

    String getWorklogTypeId();

    void setWorklogTypeId(String worklogTypeId);

    @NotNull
    Long getRatedTime();

    void setRatedTime(Long ratedTime);

    boolean getActive();

    void setActive(boolean active);
}
