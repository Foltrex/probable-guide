package com.scn.jira.worklog.core.wl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;

public class ExtWorklog extends WorklogImpl {
    private String worklogTypeId;

    public ExtWorklog(@Qualifier("overridedWorklogManager") WorklogManager worklogManager, Issue issue, String author, String comment, Date startDate, String groupLevel, Long roleLevelId, Long timeSpent, Long id, String worklogTypeId) {
        super(worklogManager, issue, id, author, comment, startDate, groupLevel, roleLevelId, timeSpent);
        this.worklogTypeId = worklogTypeId;
    }

    public ExtWorklog(@Qualifier("overridedWorklogManager")  WorklogManager worklogManager, Worklog worklog, String worklogTypeId) {
        super(worklogManager, worklog.getIssue(), worklog.getId(), worklog.getAuthorKey(), worklog.getComment(), worklog.getStartDate(), worklog.getGroupLevel(), worklog.getRoleLevelId(), worklog.getTimeSpent());
        this.worklogTypeId = worklogTypeId;
    }

    public String getWorklogTypeId() {
        return this.worklogTypeId;
    }
}