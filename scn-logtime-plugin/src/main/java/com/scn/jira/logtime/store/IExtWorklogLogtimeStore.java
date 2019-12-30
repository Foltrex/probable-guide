package com.scn.jira.logtime.store;

import java.util.Date;
import java.util.List;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.project.Project;
import com.scn.jira.worklog.core.wl.ExtWorklog;

public interface IExtWorklogLogtimeStore {
	
	List<ExtWorklog> getExtWorklogsByProjectBetweenDates(boolean assignedCh, Project project, Date startDate, Date endDate, String user) throws DataAccessException;
	
	List<ExtWorklog> getWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate) throws DataAccessException;
	
	List<ExtWorklog> getWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate, String user) throws DataAccessException;
	
	List<Issue> getIssuesByProjects(Project project) throws DataAccessException;
	
	List<Long> getProjectIdsWithExtWorklogsBetweenDates(List<Long> projectIds, List<String> users, Date startDate, Date endDate) throws DataAccessException;
	
	boolean deleteExtWorklogById(Long worklogId) throws DataAccessException;
	
	void updateExtWorklog(Long _worklogId, String _worklogType, Long _timeSpent, String _comment) throws DataAccessException;
	
	Worklog createExtWorklog(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date)
			throws DataAccessException;
	
	Worklog getExtWorklogObj(Long _worklogId, Long issueId) throws DataAccessException;
	
}