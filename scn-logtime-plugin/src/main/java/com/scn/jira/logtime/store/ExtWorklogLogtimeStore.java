package com.scn.jira.logtime.store;

import static org.ofbiz.core.entity.EntityOperator.EQUALS;
import static org.ofbiz.core.entity.EntityOperator.GREATER_THAN_EQUAL_TO;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN_EQUAL_TO;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.Lists;
import com.scn.jira.worklog.core.wl.ExtWorklog;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.ExtendedWorklogManager;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import com.scn.jira.logtime.util.DateUtils;


public class ExtWorklogLogtimeStore  implements IExtWorklogLogtimeStore {
	
	public static final String EXT_WORKLOG_LOGTIME_ENTITY = "ExtWorklogByIssueView";
	
	public static final String VIEW_EXT_WORKLOG_LOGTIME_ENTITY = "ViewExtWorklogByProject";
	public static final String EXT_WORKLOG_PROJECT_ENTITY = "ExtWorklogByProjectView";
	public static final String ISSUE_PROJECT_ENTITY = "IssueByProjectView";

	private UserManager userManager;
    private ProjectManager projectManager;
    private IssueManager issueManager;
    private PermissionManager permissionManager;
    private ProjectRoleManager projectRoleManager;
    private WorklogManager worklogManager;
    private ExtendedConstantsManager extendedConstantsManager;
    private ExtendedWorklogManager extendedWorklogManager;
	private IScnWorklogService scnDefaultWorklogService;
    
    public ExtWorklogLogtimeStore(UserManager userManager,ProjectManager projectManager,IssueManager issueManager, 
            PermissionManager permissionManager,ProjectRoleManager projectRoleManager,WorklogManager worklogManager,
            ExtendedConstantsManager extendedConstantsManager,  ExtendedWorklogManager extendedWorklogManager,IScnWorklogService scnDefaultWorklogService)
		{
		this.userManager = userManager;
		this.projectManager = projectManager;
		this.issueManager= issueManager;		
		this.permissionManager = permissionManager;		
		this.projectRoleManager=projectRoleManager;
		this.worklogManager= worklogManager;
		this.extendedConstantsManager= extendedConstantsManager;
		this.extendedWorklogManager=  extendedWorklogManager;
		this.scnDefaultWorklogService= scnDefaultWorklogService;	
		}

	public List<ExtWorklog> getExtWorklogsByProjectBetweenDates(boolean assignedCh,Project project, Date startDate, Date endDate,String user) throws DataAccessException{
		List<EntityCondition> conditions = Lists.newArrayList();		
		endDate= DateUtils.getStartDate(1, endDate);	
		conditions.add(new EntityExpr("startdate", LESS_THAN, new java.sql.Date(endDate.getTime())));
		conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new java.sql.Date(startDate.getTime())));
		conditions.add(new EntityExpr("projectId", EQUALS, project.getId()));
		conditions.add(new EntityExpr("author", EQUALS, user.trim()));
		EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);		
		
		List<GenericValue> worklogExtGVs = ComponentAccessor.getOfBizDelegator().findByCondition(EXT_WORKLOG_PROJECT_ENTITY, conditionList, null, EasyList.build("created ASC"));
	//	List<GenericValue> worklogExtGVs = ComponentAccessor.getOfBizDelegator().findByCondition(VIEW_EXT_WORKLOG_LOGTIME_ENTITY, conditionList, null, EasyList.build("created ASC"));

		List<ExtWorklog> extWorklogs = new ArrayList<ExtWorklog>();
		Iterator<GenericValue> worklogGVsExtIterator = worklogExtGVs.iterator();		
		
		while (worklogGVsExtIterator.hasNext()) {
			GenericValue genericWorklog = (GenericValue) worklogGVsExtIterator.next();
			
			if(genericWorklog!=null){
				Issue issue = issueManager.getIssueObject(genericWorklog.getLong("issue"));
				if (assignedCh && issue!=null &&  issue.getAssignee()!=null && issue.getAssignee().getName().equals(user)
						|| !assignedCh) {
					ExtWorklog extWorklog = convertToExtWorklog(worklogManager, genericWorklog, issue);
					extWorklogs.add(extWorklog);
					
				}
			}
		}
		
		return extWorklogs;
    }
    
	public List<ExtWorklog> getWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate) throws DataAccessException{
		List<EntityCondition> conditions = Lists.newArrayList();
		conditions.add(new EntityExpr("startdate", LESS_THAN_EQUAL_TO, new java.sql.Date(endDate.getTime())));
		conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new java.sql.Date(startDate.getTime())));
		conditions.add(new EntityExpr("issueId", EQUALS, issue.getId()));
		EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);		
		
		List<GenericValue> worklogExtGVs = ComponentAccessor.getOfBizDelegator().findByCondition(EXT_WORKLOG_LOGTIME_ENTITY, conditionList, null, EasyList.build("created ASC"));
	
		List<ExtWorklog> extWorklogs = new ArrayList<ExtWorklog>();
		Iterator<GenericValue> worklogGVsExtIterator = worklogExtGVs.iterator();		
		while (worklogGVsExtIterator.hasNext()) {
			GenericValue genericWorklog = (GenericValue) worklogGVsExtIterator.next();
			if(genericWorklog!=null){
				ExtWorklog extWorklog = convertToExtWorklog(worklogManager, genericWorklog, issue);
				extWorklogs.add(extWorklog);
			}
		}
			
		return extWorklogs;
    }
	
	public List<ExtWorklog> getWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate, String user) throws DataAccessException{
		List<EntityCondition> conditions = Lists.newArrayList();
		conditions.add(new EntityExpr("startdate", LESS_THAN_EQUAL_TO, new java.sql.Date(endDate.getTime())));
		conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new java.sql.Date(startDate.getTime())));
		conditions.add(new EntityExpr("issueId", EQUALS, issue.getId()));
		conditions.add(new EntityExpr("author", EQUALS, user));
		EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);		
		
		List<GenericValue> worklogExtGVs = ComponentAccessor.getOfBizDelegator().findByCondition(EXT_WORKLOG_LOGTIME_ENTITY, conditionList, null, EasyList.build("created ASC"));
	
		List<ExtWorklog> extWorklogs = new ArrayList<ExtWorklog>();
		Iterator<GenericValue> worklogGVsExtIterator = worklogExtGVs.iterator();		
		while (worklogGVsExtIterator.hasNext()) {
			GenericValue genericWorklog = (GenericValue) worklogGVsExtIterator.next();
			if(genericWorklog!=null){
				ExtWorklog extWorklog = convertToExtWorklog(worklogManager, genericWorklog, issue);
				extWorklogs.add(extWorklog);
			}
		}
			
		return extWorklogs;
    }
	
	public List<Issue> getIssuesByProjects(Project project) throws DataAccessException{
		
		List<EntityCondition> conditions2 = new ArrayList<EntityCondition>();
		conditions2.add(new EntityExpr("projectId", EQUALS, project.getId()));
		
		List<GenericValue> worklogGVs2 = ComponentAccessor.getOfBizDelegator().findByAnd(ISSUE_PROJECT_ENTITY, conditions2);
		
		List<Issue> issues = new ArrayList<Issue>();
		
		
		Iterator<GenericValue>worklogGVsExtIterator = worklogGVs2.iterator();
		while (worklogGVsExtIterator.hasNext()) {
			GenericValue genericWorklog = (GenericValue) worklogGVsExtIterator.next();
			Issue issue = this.issueManager.getIssueObject(genericWorklog.getLong("id"));
			issues.add(issue);
		}	
		return issues;
    }
	
	public GenericValue getExtWorklog(Long _worklogId) throws DataAccessException
		{
			List worklogs = ComponentAccessor.getOfBizDelegator().findByAnd("WorklogExt", UtilMisc.toMap("id", _worklogId));
			
			GenericValue gv = null;
			if (worklogs != null && worklogs.size() > 0)
			{
				gv = (GenericValue) worklogs.get(0);
			}
			return gv;
	}
	
	public Worklog getExtWorklogObj(Long _worklogId, Long issueId) throws DataAccessException{
		
		Worklog worklog = convertToWorklog( worklogManager,  _worklogId,  issueId);
		
		return worklog;
	}
	
	
	public Worklog createExtWorklog(Long issueId, String _worklogType, Long _timeSpent,
			String _comment, String authorKey, Date date) throws DataAccessException{
		
		Worklog worklog = formWorklog(issueId, _timeSpent, null, _comment,  authorKey,date);
		final Map fields = createLinkedParamMap(worklog);
		final GenericValue worklogGV = ComponentAccessor.getOfBizDelegator().createValue("Worklog", fields);
		
		Worklog worklogCreated = formWorklog(issueId, _timeSpent, worklogGV.getLong("id"), _comment,  authorKey,date);
		
		extendedWorklogManager.createExtWorklogType(worklogCreated, _worklogType);
		return worklogCreated;
	}
	
	
	
	public void updateExtWorklog( Long _worklogId, String _worklogType, Long _timeSpent, String _comment) throws DataAccessException
	{
		GenericValue worklogGV = ComponentAccessor.getOfBizDelegator().findByPrimaryKey("Worklog", EasyMap.build("id",_worklogId));
		if (worklogGV == null)
			throw new DataAccessException("Could not find original worklog entity to update.");
		
		worklogGV.set("timeworked", _timeSpent);
		if(_comment!=null){
			worklogGV.set("body", _comment);
		}
		
		try
		{
			worklogGV.store();
		}
		catch (GenericEntityException e)
		{
			throw new DataAccessException("Could not store the worklog  to update.", e);
		}
		
		extendedWorklogManager.updateExtWorklogType(_worklogId, _worklogType);
		
	}
	
	
	public boolean deleteExtWorklogById(Long worklogId) throws DataAccessException
	{
		int i = ComponentAccessor.getOfBizDelegator().removeById("WorklogExt", worklogId);
		int j = ComponentAccessor.getOfBizDelegator().removeById("Worklog", worklogId);
		return (i == 1&&j==1);
	}
	
	
	public Worklog formWorklog(Long issueId, Long _timeSpent,Long id,
			String _comment, String authorKey, Date date){
				Issue issue = this.issueManager.getIssueObject(issueId);
		Worklog wl = new WorklogImpl(this.worklogManager,issue,id,authorKey,
				_comment,date,null,null,_timeSpent,	authorKey,date,date);		
		return wl;	
	}
	
	
	public Worklog convertToWorklog(WorklogManager worklogManager, Long wlId,Long issueId)
   	{   
   	    GenericValue gExw = ComponentAccessor.getOfBizDelegator().findByPrimaryKey("Worklog", EasyMap.build("id",wlId));
		if(gExw==null){
			return null;
		}
   	    Issue issue = this.issueManager.getIssueObject(issueId);   		
   		Worklog wl = new WorklogImpl(this.worklogManager,issue,wlId,gExw.getString("author"),
   				gExw.getString("body"),gExw.getTimestamp("startdate"),gExw.getString("grouplevel"),gExw.getLong("rolelevel"),gExw.getLong("timeworked"),gExw.getString("author"),gExw.getTimestamp("startdate"),gExw.getTimestamp("startdate"));	
   	
   		return wl;
   	}
	
	public ExtWorklog convertToExtWorklog(WorklogManager worklogManager, GenericValue gv, Issue issue)
   	{
   		Timestamp startDateTS = gv.getTimestamp("startdate");   	
   		
   	    GenericValue gExw =  getExtWorklog(gv.getLong("id"));
   		   		
   	    String worklogType = (gExw.getString("worklogtype")==null || gExw.getString("worklogtype")=="")?"0":gExw.getString("worklogtype");
   	    ExtWorklog worklogExt =  new ExtWorklog(worklogManager,
   				issue,
   				gv.getString("author"),
   				gv.getString("body"), 
   				startDateTS,
   				gv.getString("grouplevel"), 
   				gv.getLong("rolelevel"),
   				gv.getLong("timeworked"),
   				gv.getLong("id"),
   				worklogType);
   	
   		return worklogExt;
   	}
	
	 
	  
	  protected Map<String, Object> createLinkedParamMap(Worklog worklog)
		{
	
			Map<String, Object> fields = new HashMap();
			fields.put("issue", worklog.getIssue().getId());
			fields.put("author", worklog.getAuthor());
			fields.put("updateauthor", worklog.getUpdateAuthor());
			fields.put("body", worklog.getComment());
			fields.put("grouplevel", worklog.getGroupLevel());
			fields.put("rolelevel", worklog.getRoleLevelId());
			fields.put("timeworked", worklog.getTimeSpent());
			fields.put("startdate", new Timestamp(worklog.getStartDate().getTime()));
			fields.put("created", new Timestamp(worklog.getCreated().getTime()));
			fields.put("updated", new Timestamp(worklog.getUpdated().getTime()));
			return fields;
		}
	  
	  
}