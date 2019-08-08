package com.scn.jira.logtime.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.logtime.representation.LTProjectRepresentation;

public interface IWorklogLogtimeManager {
	LTProjectRepresentation getLTProjectRepresentationBetweenDates(ApplicationUser loggedUser, Project project,
			Date startDate, Date endDate, boolean scnWlCheck, boolean extWlCheck, boolean assignedCh, String user)
			throws DataAccessException;

	public List<Long> getProjectIdsWithScnWorklogsBetweenDates(List<Long> projectIds, List<String> users, Date startDate, Date endDate)
			throws DataAccessException;
	
	public List<Long> getProjectIdsWithExtWorklogsBetweenDates(List<Long> projectIds, List<String> users, Date startDate, Date endDate)
			throws DataAccessException;

	void setCalendarMap(Map<String, Map<String, Integer>> calendarMap);
}