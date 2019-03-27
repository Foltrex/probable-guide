package com.scn.jira.mytime.store;

import static org.ofbiz.core.entity.EntityOperator.EQUALS;
import static org.ofbiz.core.entity.EntityOperator.GREATER_THAN_EQUAL_TO;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;
import org.springframework.beans.factory.annotation.Qualifier;

public class ScnWorklogMyTimeStore {
	
	public static final String SCN_WORKLOG_ISSUE_ENTITY = "ScnWorklogByIssueView";
	
	private IssueManager issueManager;
	private ProjectRoleManager projectRoleManager;
	private WorklogManager worklogManager;
	
	public ScnWorklogMyTimeStore(IssueManager issueManager, ProjectRoleManager projectRoleManager, @Qualifier("overridedWorklogManager")WorklogManager worklogManager) {
		this.issueManager = issueManager;
		this.projectRoleManager = projectRoleManager;
		this.worklogManager = worklogManager;
		
	}
	
	public List<IScnWorklog> getScnWorklogsByUserBetweenDates(Date startDate, Date endDate, String user) throws DataAccessException {
		
		List<EntityCondition> conditions1 = new ArrayList<EntityCondition>();
		conditions1.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
		conditions1.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
		conditions1.add(new EntityExpr("author", EQUALS, user.trim()));
		EntityCondition conditionList = new EntityConditionList(conditions1, EntityOperator.AND);
		
		List<GenericValue> worklogGVs = ComponentAccessor.getOfBizDelegator().findByCondition(SCN_WORKLOG_ISSUE_ENTITY, conditionList, null,
				EasyList.build("created ASC"));
		
		List<IScnWorklog> iScnWorklogs = new ArrayList<IScnWorklog>();
		Iterator worklogGVsIterator = worklogGVs.iterator();
		while (worklogGVsIterator.hasNext()) {
			GenericValue genericWorklog = (GenericValue) worklogGVsIterator.next();
			IScnWorklog iScnWorklog = convertToWorklog(projectRoleManager, genericWorklog);
			iScnWorklogs.add(iScnWorklog);
			
		}
		return iScnWorklogs;
	}
	
	public IScnWorklog convertToWorklog(ProjectRoleManager projectRoleManager, GenericValue gv) {
		Timestamp startDateTS = gv.getTimestamp("startdate");
		Timestamp createdTS = gv.getTimestamp("created");
		Timestamp updatedTS = gv.getTimestamp("updated");
		
		IScnWorklog worklog = new ScnWorklogImpl(projectRoleManager, issueManager.getIssueObject(gv.getLong("issue")), gv.getLong("id"),
				gv.getString("author"), gv.getString("body"), startDateTS != null ? new Date(startDateTS.getTime()) : null,
				gv.getString("grouplevel"), gv.getLong("rolelevel"), gv.getLong("timeworked"), gv.getString("updateauthor"),
				createdTS != null ? new Date(createdTS.getTime()) : null, updatedTS != null ? new Date(updatedTS.getTime()) : null,
				gv.getString("worklogtype"));
		
		Long linkedWorklogId = gv.getLong("linkedWorklog");
		worklog.setLinkedWorklog(linkedWorklogId == null ? null : worklogManager.getById(linkedWorklogId));
		
		return worklog;
	}
	
}
