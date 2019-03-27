package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.issue.Issue;

public class ScnExtendedIssue implements IScnExtendedIssue{

    private final Issue issue;
    private final Long id;
    private final Long originalEstimate;
    private final Long estimate;
    private final Long timeSpent;

    public ScnExtendedIssue(Issue issue, Long id, Long originalEstimate, Long estimate, Long timeSpent) {
        this.issue = issue;
        this.id = id;
        this.originalEstimate = originalEstimate;
        this.estimate = estimate;
        this.timeSpent = timeSpent;
    }

    public Issue getIssue() {
        return issue;
    }

    public Long getId() {
        return id;
    }

    public Long getOriginalEstimate() {
        return originalEstimate;
    }

    public Long getEstimate() {
        return estimate;
    }

    public Long getTimeSpent() {
        return timeSpent;
    }
}
