package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.issue.Issue;

public interface IScnExtendedIssue {
    public abstract Long getId();
    public abstract Issue getIssue();
    public abstract Long getOriginalEstimate();
    public abstract Long getEstimate();
    public abstract Long getTimeSpent();
}
