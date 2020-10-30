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
	
	List<IScnWorklog> getByProjectBetweenDates(boolean assignedCh, Project project, Date startDate, Date endDate, String user) throws DataAccessException;
	
	List<IScnWorklog> getScnWorklogsByUserAndIssueBetweenDates(Issue issue, Date startDate, Date endDate, String user)
			throws DataAccessException;
	
	List<IScnWorklog> getScnWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate) throws DataAccessException;
	
	List<Long> getProjectIdsWithScnWorklogsBetweenDates(List<Long> projectIds, List<String> users, Date startDate, Date endDate) throws DataAccessException;
	
	boolean deleteScnWorklogById(Long worklogId) throws DataAccessException;
	
	boolean updateScnWorklog(Long _worklogId, Worklog linkedWorklog) throws DataAccessException;

	boolean updateScnWorklogAndExt(Long _worklogId, Worklog linkedWorklog) throws DataAccessException;

	boolean updateScnWorklog(Long _worklogId, String _worklogType, Long _timeSpent, String _comment) throws DataAccessException;
	
	boolean createScnWorklog(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date,
							 String worklogTypeId) throws DataAccessException;
	
	Map<String, Object> createScnWorklogResultMap(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date,
												  String worklogTypeId) throws DataAccessException;
	
	IScnWorklog createScnWorklogWithoutCopy(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date,
											String worklogTypeId) throws DataAccessException;
	
	IScnWorklog getScnWorklog(Long _worklogId) throws DataAccessException;
	
	boolean isProjectWLBlocked(Long projectId, Date date);
	
	boolean isProjectWLWorklogBlocked(Long projectId, Date date);
	
	boolean isWlAutoCopy(Issue issue, String worklogTypeId);

    boolean isWLTypeRequired(Long projectId);
}
