package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Khadarovich
 * Date: 09.08.2010
 * Time: 12:17:34
 * To change this template use File | Settings | File Templates.
 */
public interface IScnWorklogStore {
    public static final String SCN_WORKLOG_ENTITY = "ScnWorklog";
    public static final String WORKLOG_ENTITY = "Worklog";
    
    boolean deleteLinkedWorklog(Long linkedWorklogId) throws DataAccessException;

    IScnWorklog update(IScnWorklog worklog, boolean isLinkedWL) throws DataAccessException;

    IScnWorklog create(IScnWorklog worklog, boolean isLinkedWL) throws DataAccessException;

    boolean delete(Long long1, boolean isLinkedWL) throws DataAccessException;
    
    int deleteAllByIssueId(Long issueId);

    IScnWorklog getById(Long long1) throws DataAccessException;

    List<IScnWorklog> getByIssue(Issue issue) throws DataAccessException;

    List<IScnWorklog> getByProject(Project project) throws DataAccessException;

    List<IScnWorklog> getByProjectBetweenDates(Project project, Date startDate, Date endDate) throws DataAccessException;

    long getCountForWorklogsRestrictedByGroup(String s) throws DataAccessException;

    int swapWorklogGroupRestriction(String s, String s1) throws DataAccessException;

    List<IScnWorklog> getScnWorklogsByType(String worklogTypeId) throws DataAccessException;
}
