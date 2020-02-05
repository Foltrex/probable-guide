package com.scn.jira.logtime.store;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.worklog.core.wl.ExtWorklog;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.*;
import org.ofbiz.core.util.UtilMisc;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.ofbiz.core.entity.EntityOperator.*;

public class ExtWorklogLogtimeStore implements IExtWorklogLogtimeStore {
    private static final Logger LOGGER = Logger.getLogger(ExtWorklogLogtimeStore.class);

    public static final String EXT_WORKLOG_LOGTIME_ENTITY = "ExtWorklogByIssueView";
    public static final String EXT_WORKLOG_PROJECT_ENTITY = "ExtWorklogByProjectView";

    private IssueManager issueManager;
    private WorklogManager worklogManager;

    public ExtWorklogLogtimeStore(IssueManager issueManager, WorklogManager worklogManager) {
        this.issueManager = issueManager;
        this.worklogManager = worklogManager;
    }

    public List<ExtWorklog> getExtWorklogsByProjectBetweenDates(boolean assignedCh, @Nonnull Project project, @Nonnull Date startDate,
                                                                @Nonnull Date endDate, @Nonnull String user) throws DataAccessException {
        List<EntityCondition> conditions = Lists.newArrayList();
        endDate = DateUtils.getStartDate(1, endDate);
        conditions.add(new EntityExpr("startdate", LESS_THAN, new java.sql.Date(endDate.getTime())));
        conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new java.sql.Date(startDate.getTime())));
        conditions.add(new EntityExpr("projectId", EQUALS, project.getId()));
        conditions.add(new EntityExpr("author", EQUALS, user.trim()));
        EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);

        List<GenericValue> worklogExtGVs = ComponentAccessor.getOfBizDelegator()
            .findByCondition(EXT_WORKLOG_PROJECT_ENTITY, conditionList, null, ImmutableList.of("created ASC"));
        // List<GenericValue> worklogExtGVs =
        // ComponentAccessor.getOfBizDelegator().findByCondition(VIEW_EXT_WORKLOG_LOGTIME_ENTITY,
        // conditionList, null, EasyList.build("created ASC"));

        List<ExtWorklog> extWorklogs = new ArrayList<>();

        List<Long> issueIds = new ArrayList<>();
        for (GenericValue worklogGV : worklogExtGVs) {
            issueIds.add(worklogGV.getLong("issue"));
        }

        List<Issue> issueObjects = issueManager.getIssueObjects(issueIds);
        Map<Long, Issue> issueObjectsMap = new HashMap<>();

        for (Issue issueObject : issueObjects) {
            issueObjectsMap.put(issueObject.getId(), issueObject);
        }

        for (GenericValue worklogGV : worklogExtGVs) {
            if (worklogGV != null) {
                Issue issue = issueObjectsMap.get(worklogGV.getLong("issue"));
                if (!assignedCh || issue != null && issue.getAssignee() != null
                    && user.equals(issue.getAssignee().getName())) {
                    ExtWorklog extWorklog = convertToExtWorklog(worklogManager, worklogGV, issue);
                    extWorklogs.add(extWorklog);
                }
            }
        }

        return extWorklogs;
    }

    public List<ExtWorklog> getWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate)
        throws DataAccessException {
        List<EntityCondition> conditions = Lists.newArrayList();
        conditions.add(new EntityExpr("startdate", LESS_THAN_EQUAL_TO, new java.sql.Date(endDate.getTime())));
        conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new java.sql.Date(startDate.getTime())));
        conditions.add(new EntityExpr("issueId", EQUALS, issue.getId()));
        EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);

        List<GenericValue> worklogExtGVs = ComponentAccessor.getOfBizDelegator()
            .findByCondition(EXT_WORKLOG_LOGTIME_ENTITY, conditionList, null, ImmutableList.of("created ASC"));

        List<ExtWorklog> extWorklogs = new ArrayList<>();
        for (GenericValue genericWorklog : worklogExtGVs) {
            if (genericWorklog != null) {
                ExtWorklog extWorklog = convertToExtWorklog(worklogManager, genericWorklog, issue);
                extWorklogs.add(extWorklog);
            }
        }

        return extWorklogs;
    }

    public List<ExtWorklog> getWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate, String user)
        throws DataAccessException {
        List<EntityCondition> conditions = Lists.newArrayList();
        conditions.add(new EntityExpr("startdate", LESS_THAN_EQUAL_TO, new java.sql.Date(endDate.getTime())));
        conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new java.sql.Date(startDate.getTime())));
        conditions.add(new EntityExpr("issueId", EQUALS, issue.getId()));
        conditions.add(new EntityExpr("author", EQUALS, user));
        EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);

        List<GenericValue> worklogExtGVs = ComponentAccessor.getOfBizDelegator()
            .findByCondition(EXT_WORKLOG_LOGTIME_ENTITY, conditionList, null, ImmutableList.of("created ASC"));

        List<ExtWorklog> extWorklogs = new ArrayList<>();
        for (GenericValue genericWorklog : worklogExtGVs) {
            if (genericWorklog != null) {
                ExtWorklog extWorklog = convertToExtWorklog(worklogManager, genericWorklog, issue);
                extWorklogs.add(extWorklog);
            }
        }

        return extWorklogs;
    }

    public List<Issue> getIssuesByProjects(Project project) throws DataAccessException {
        try {
            return this.issueManager.getIssueObjects(issueManager.getIssueIdsForProject(project.getId()));
        } catch (GenericEntityException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public GenericValue getExtWorklog(Long _worklogId) throws DataAccessException {
        List<GenericValue> worklogs = ComponentAccessor.getOfBizDelegator().findByAnd("WorklogExt",
            UtilMisc.toMap("id", _worklogId));

        GenericValue gv = null;
        if (worklogs != null && worklogs.size() > 0) {
            gv = worklogs.get(0);
        }
        return gv;
    }

    @Override
    public List<Long> getProjectIdsWithExtWorklogsBetweenDates(List<Long> projectIds, List<String> users, Date startDate, Date endDate) throws DataAccessException {
        List<EntityCondition> conditions = new ArrayList<>();
        conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
        conditions.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
        conditions.add(new EntityExpr("projectId", IN, projectIds));
        conditions.add(new EntityExpr("author", IN, users));
        EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);

        return ComponentAccessor.getOfBizDelegator()
            .findByCondition(EXT_WORKLOG_PROJECT_ENTITY, conditionList, ImmutableList.of("projectId")).stream()
            .map(x -> x.getLong("projectId")).distinct().collect(Collectors.toList());
    }

    @Nonnull
    public ExtWorklog convertToExtWorklog(WorklogManager worklogManager, GenericValue gv, Issue issue) {
        Timestamp startDateTS = gv.getTimestamp("startdate");
        String worklogType = "0";
        GenericValue gExw = getExtWorklog(gv.getLong("id"));
        if (gExw != null && gExw.getString("worklogtype") != null && !"".equals(gExw.getString("worklogtype"))) {
            worklogType = gExw.getString("worklogtype");
        }

        return new ExtWorklog(worklogManager, issue, gv.getString("author"), gv.getString("body"),
            startDateTS, gv.getString("grouplevel"), gv.getLong("rolelevel"), gv.getLong("timeworked"),
            gv.getLong("id"), worklogType);
    }
}
