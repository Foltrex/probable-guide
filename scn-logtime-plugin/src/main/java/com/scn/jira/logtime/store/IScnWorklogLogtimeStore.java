package com.scn.jira.logtime.store;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.project.Project;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;

public interface IScnWorklogLogtimeStore {
	
	public List<IScnWorklog> getByProjectBetweenDates(boolean assignedCh,Project project, Date startDate, Date endDate,String user) throws DataAccessException;
	
	public List<IScnWorklog> getScnWorklogsByUserAndIssueBetweenDates(Issue issue, Date startDate, Date endDate, String user)
			throws DataAccessException;
	
	public List<IScnWorklog> getScnWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate) throws DataAccessException;
	
	public boolean deleteScnWorklogById(Long worklogId) throws DataAccessException;
	
	public boolean updateScnWorklog(Long _worklogId, Worklog linkedWorklog) throws DataAccessException;

	public boolean updateScnWorklogAndExt(Long _worklogId, Worklog linkedWorklog) throws DataAccessException;

	public boolean updateScnWorklog(Long _worklogId, String _worklogType, Long _timeSpent, String _comment) throws DataAccessException;
	
	public boolean createScnWorklog(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date,
			String worklogTypeId) throws DataAccessException;
	
	public Map<String, Object> createScnWorklogResultMap(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date,
			String worklogTypeId) throws DataAccessException;
	
	public IScnWorklog createScnWorklogWithoutCopy(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date,
			String worklogTypeId) throws DataAccessException;
	
	public IScnWorklog getScnWorklog(Long _worklogId) throws DataAccessException;
	
	public boolean isProjectWLBlocked(Long projectId, Date date);
	
	public boolean isProjectWLWorklogBlocked(Long projectId, Date date);
	
	public boolean isWlAutoCopy(Issue issue, String worklogTypeId);
	
}
