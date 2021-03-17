package com.scn.jira.mytime.store;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.google.common.collect.ImmutableList;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;
import lombok.RequiredArgsConstructor;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import javax.inject.Named;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.ofbiz.core.entity.EntityOperator.EQUALS;
import static org.ofbiz.core.entity.EntityOperator.GREATER_THAN_EQUAL_TO;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN;

@Named
@RequiredArgsConstructor
public class ScnWorklogMyTimeStore {
    public static final String SCN_WORKLOG_ISSUE_ENTITY = "ScnWorklogByIssueView";

    private final IssueManager issueManager;
    private final ProjectRoleManager projectRoleManager;
    private final WorklogManager worklogManager;

    public List<IScnWorklog> getScnWorklogsByUserBetweenDates(Date startDate, Date endDate, String user) throws DataAccessException {
        List<EntityCondition> conditions1 = new ArrayList<>();
        conditions1.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
        conditions1.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
        conditions1.add(new EntityExpr("author", EQUALS, user.trim()));
        EntityCondition conditionList = new EntityConditionList(conditions1, EntityOperator.AND);

        List<GenericValue> worklogGVs = ComponentAccessor.getOfBizDelegator().findByCondition(SCN_WORKLOG_ISSUE_ENTITY,
            conditionList, null, ImmutableList.of("created ASC"));

        List<IScnWorklog> iScnWorklogs = new ArrayList<>();
        for (GenericValue genericWorklog : worklogGVs) {
            IScnWorklog iScnWorklog = convertToWorklog(projectRoleManager, genericWorklog);
            iScnWorklogs.add(iScnWorklog);
        }
        return iScnWorklogs;
    }

    public IScnWorklog convertToWorklog(ProjectRoleManager projectRoleManager, GenericValue gv) {
        Timestamp startDateTS = gv.getTimestamp("startdate");
        Timestamp createdTS = gv.getTimestamp("created");
        Timestamp updatedTS = gv.getTimestamp("updated");

        IScnWorklog worklog = new ScnWorklogImpl(projectRoleManager, issueManager.getIssueObject(gv.getLong("issue")),
            gv.getLong("id"), gv.getString("author"), gv.getString("body"),
            startDateTS != null ? new Date(startDateTS.getTime()) : null, gv.getString("grouplevel"),
            gv.getLong("rolelevel"), gv.getLong("timeworked"), gv.getString("updateauthor"),
            createdTS != null ? new Date(createdTS.getTime()) : null,
            updatedTS != null ? new Date(updatedTS.getTime()) : null, gv.getString("worklogtype"));

        Long linkedWorklogId = gv.getLong("linkedWorklog");
        worklog.setLinkedWorklog(linkedWorklogId == null ? null : worklogManager.getById(linkedWorklogId));

        return worklog;
    }

}
