package com.scn.jira.logtime.store;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.scn.jira.worklog.core.wl.ExtWorklog;

import java.util.Date;
import java.util.List;

public interface IExtWorklogLogtimeStore {
    List<ExtWorklog> getExtWorklogsByProjectBetweenDates(boolean assignedCh, Project project, Date startDate, Date endDate, String user) throws DataAccessException;

    List<ExtWorklog> getWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate) throws DataAccessException;

    List<ExtWorklog> getWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate, String user) throws DataAccessException;

    List<Issue> getIssuesByProjects(Project project) throws DataAccessException;

    List<Long> getProjectIdsWithExtWorklogsBetweenDates(List<Long> projectIds, List<String> users, Date startDate, Date endDate) throws DataAccessException;
}
