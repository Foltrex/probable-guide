package com.scn.jira.worklog.core.wl;

import com.atlassian.jira.issue.IssueConstant;

public abstract interface WorklogType extends IssueConstant
{
  public static final String JIRA_CONSTANT_DEFAULT_WORKLOGTYPE = "jira.constant.default.worklogtype";
}