package com.scn.jira.worklog.impl.domain.entity;

import com.atlassian.jira.issue.Issue;
import com.scn.jira.common.ao.Updatable;
import java.util.Date;
import net.java.ao.Preload;
import net.java.ao.schema.Index;
import net.java.ao.schema.Indexes;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.PrimaryKey;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Unique;

@Preload
@Indexes(@Index(
    name = "author_start_date_issue_id_worklog_id",
    methodNames = {"getAuthorKey", "getStartDate", "getIssueId"}
))
public interface ScnWorklog extends Updatable {

    @Override
    @PrimaryKey(ID)
    Long getId();

    @Unique
    Long getWorklogId();

    WLType getWorklogType();

    @NotNull
    Date getStartDate();

    @NotNull
    Long getTimeSpent();

    String getGroupLevel();

    Long getRoleLevelId();

    @StringLength(StringLength.UNLIMITED)
    String getComment();

    @NotNull
    Long getIssueId();
}
