package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Date;
import java.util.List;

public interface IScnWorklogManager {
	boolean delete(ApplicationUser paramUser, IScnWorklog paramWorklog, Long paramLong, Long newLinkedEstimate,
			boolean paramBoolean, boolean isLinkedWL) throws DataAccessException;

	IScnWorklog update(ApplicationUser paramUser, IScnWorklog paramWorklog, Long paramLong, Long newLinkedEstimate,
			boolean paramBoolean, boolean isLinkedWL) throws DataAccessException;

	IScnWorklog create(ApplicationUser paramUser, IScnWorklog paramWorklog, Long paramLong, Long newLinkedEstimate,
			boolean paramBoolean, boolean isLinkedWL) throws DataAccessException;

	IScnWorklog getById(Long paramLong) throws DataAccessException;

	List<IScnWorklog> getByIssue(Issue paramIssue) throws DataAccessException;

	List<IScnWorklog> getByProjectBetweenDates(Project project, Date startDate, Date endDate)
			throws DataAccessException;

	List<IScnWorklog> getByProject(Project project) throws DataAccessException;

	long getCountForWorklogsRestrictedByGroup(String paramString) throws DataAccessException;

	int swapWorklogGroupRestriction(String paramString1, String paramString2) throws DataAccessException;

	void validateWorklog(IScnWorklog worklog, boolean create);

	List<IScnWorklog> getScnWorklogsByType(String worklogTypeId) throws DataAccessException;

	IScnWorklog update(IScnWorklog worklog) throws DataAccessException;
}
