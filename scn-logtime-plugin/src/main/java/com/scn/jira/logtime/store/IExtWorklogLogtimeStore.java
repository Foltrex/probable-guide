package com.scn.jira.logtime.store;

import java.util.Date;
import java.util.List;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.project.Project;
import com.scn.jira.worklog.core.wl.ExtWorklog;

public interface IExtWorklogLogtimeStore {
	
	public List<ExtWorklog> getExtWorklogsByProjectBetweenDates(boolean assignedCh, Project project, Date startDate, Date endDate,String user) throws DataAccessException;
	
	public List<ExtWorklog> getWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate) throws DataAccessException;
	
	public List<ExtWorklog> getWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate, String user) throws DataAccessException;
	
	public List<Issue> getIssuesByProjects(Project project) throws DataAccessException;
	
	public boolean deleteExtWorklogById(Long worklogId) throws DataAccessException;
	
	public void updateExtWorklog(Long _worklogId, String _worklogType, Long _timeSpent, String _comment) throws DataAccessException;
	
	public Worklog createExtWorklog(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date)
			throws DataAccessException;
	
	public Worklog getExtWorklogObj(Long _worklogId, Long issueId) throws DataAccessException;
	
}