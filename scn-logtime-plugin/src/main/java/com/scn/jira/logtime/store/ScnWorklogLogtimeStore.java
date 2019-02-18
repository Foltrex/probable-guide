package com.scn.jira.logtime.store;

import static org.ofbiz.core.entity.EntityOperator.EQUALS;
import static org.ofbiz.core.entity.EntityOperator.GREATER_THAN_EQUAL_TO;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN_EQUAL_TO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.scn.jira.logtime.util.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.Lists;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.IScnWorklogStore;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.settings.IScnUserBlockingManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import com.scn.jira.worklog.scnwl.IScnWorklogService;

public class ScnWorklogLogtimeStore /* extends OfBizScnWorklogStore */implements IScnWorklogLogtimeStore {
	
	public static final String SCN_WORKLOG_ISSUE_ENTITY = "ScnWorklogByIssueView";
	public static final String VIEW_SCN_WORKLOG_ISSUE_ENTITY = "ViewScnWorklogByProject";
	public static final String SCN_WORKLOG_PROJECT_ENTITY = "ScnWorklogByProjectView";
	
	private UserManager userManager;
	private ProjectManager projectManager;
	private IssueManager issueManager;
	private PermissionManager permissionManager;
	private ProjectRoleManager projectRoleManager;
	private WorklogManager worklogManager;
	private ExtendedConstantsManager extendedConstantsManager;
	private IScnWorklogStore ofBizScnWorklogStore;
	private IScnProjectSettingsManager projectSettignsManager;
	private IScnUserBlockingManager scnUserBlockingManager;
	private IScnWorklogService scnDefaultWorklogService;
    
	
	public ScnWorklogLogtimeStore(UserManager userManager, ProjectManager projectManager, IssueManager issueManager,
			PermissionManager permissionManager, ProjectRoleManager projectRoleManager, WorklogManager worklogManager,
			ExtendedConstantsManager extendedConstantsManager, IScnWorklogStore ofBizScnWorklogStore,
			IScnProjectSettingsManager projectSettignsManager, IScnUserBlockingManager scnUserBlockingManager,IScnWorklogService scnDefaultWorklogService) {
		this.userManager = userManager;
		this.projectManager = projectManager;
		this.issueManager = issueManager;
		this.permissionManager = permissionManager;
		this.projectRoleManager = projectRoleManager;
		this.worklogManager = worklogManager;
		this.extendedConstantsManager = extendedConstantsManager;
		this.ofBizScnWorklogStore = ofBizScnWorklogStore;
		this.projectSettignsManager = projectSettignsManager;
		this.scnUserBlockingManager = scnUserBlockingManager;
		this.scnDefaultWorklogService = scnDefaultWorklogService;
	}
	
	public List<IScnWorklog> getByProjectBetweenDates(boolean assignedCh, Project project, Date startDate, Date endDate, String user)
			throws DataAccessException {
		
		List<EntityCondition> conditions = Lists.newArrayList();
		endDate = DateUtils.getStartDate(1, endDate);
		conditions.add(new EntityExpr("startdate", LESS_THAN, new java.sql.Date(endDate.getTime())));
		conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new java.sql.Date(startDate.getTime())));
		conditions.add(new EntityExpr("projectId", EQUALS, project.getId()));
		conditions.add(new EntityExpr("author", EQUALS, user.trim()));
		EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);
		
	   List<GenericValue> worklogGVs = ComponentAccessor.getOfBizDelegator().findByCondition(SCN_WORKLOG_PROJECT_ENTITY, conditionList, null, EasyList.build("created ASC"));
	//	List<GenericValue> worklogGVs = ComponentAccessor.getOfBizDelegator().findByCondition(VIEW_SCN_WORKLOG_ISSUE_ENTITY, conditionList, null,	EasyList.build("created ASC"));
		
		List<IScnWorklog> iScnWorklogs = new ArrayList<IScnWorklog>();
		Iterator worklogGVsIterator = worklogGVs.iterator();
		while (worklogGVsIterator.hasNext()) {
			GenericValue genericWorklog = (GenericValue) worklogGVsIterator.next();
			Issue issue = issueManager.getIssueObject(genericWorklog.getLong("issue"));
			if (assignedCh && issue != null && issue.getAssignee() != null && issue.getAssignee().getName().equals(user) || !assignedCh) {
				IScnWorklog iScnWorklog = convertToWorklog(projectRoleManager, genericWorklog, issue);
				iScnWorklogs.add(iScnWorklog);
			}
			
		}
		
		return iScnWorklogs;
	}
	
	public List<IScnWorklog> getScnWorklogsByUserAndIssueBetweenDates(Issue issue, Date startDate, Date endDate, String user)
			throws DataAccessException {
		
		List<EntityCondition> conditions1 = new ArrayList<EntityCondition>();
		conditions1.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
		conditions1.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
		conditions1.add(new EntityExpr("issueId", EQUALS, issue.getId()));
		conditions1.add(new EntityExpr("author", EQUALS, user.trim()));
		EntityCondition conditionList = new EntityConditionList(conditions1, EntityOperator.AND);
		
		List<GenericValue> worklogGVs = ComponentAccessor.getOfBizDelegator().findByCondition(SCN_WORKLOG_ISSUE_ENTITY, conditionList, null,
				EasyList.build("created ASC"));
		
		List<IScnWorklog> iScnWorklogs = new ArrayList<IScnWorklog>();
		Iterator worklogGVsIterator = worklogGVs.iterator();
		while (worklogGVsIterator.hasNext()) {
			GenericValue genericWorklog = (GenericValue) worklogGVsIterator.next();
			
			IScnWorklog iScnWorklog = convertToWorklog(projectRoleManager, genericWorklog, issue);
			iScnWorklogs.add(iScnWorklog);
			
		}
		return iScnWorklogs;
	}
	
	public List<IScnWorklog> getScnWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate) throws DataAccessException {
		
		List<EntityCondition> conditions1 = new ArrayList<EntityCondition>();
		conditions1.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
		conditions1.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
		conditions1.add(new EntityExpr("issueId", EQUALS, issue.getId()));
		EntityCondition conditionList = new EntityConditionList(conditions1, EntityOperator.AND);
		
		List<GenericValue> worklogGVs = ComponentAccessor.getOfBizDelegator().findByCondition(SCN_WORKLOG_ISSUE_ENTITY, conditionList, null,
				EasyList.build("created ASC"));
		
		List<IScnWorklog> iScnWorklogs = new ArrayList<IScnWorklog>();
		Iterator worklogGVsIterator = worklogGVs.iterator();
		while (worklogGVsIterator.hasNext()) {
			GenericValue genericWorklog = (GenericValue) worklogGVsIterator.next();
			IScnWorklog iScnWorklog = convertToWorklog(projectRoleManager, genericWorklog, issue);
			iScnWorklogs.add(iScnWorklog);
		}
		return iScnWorklogs;
	}
	
	public Map<String, Object> createScnWorklogResultMap(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey,
			Date date, String worklogTypeId) throws DataAccessException {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		IScnWorklog worklog = formScnWorklog(issueId, _timeSpent, null, _comment, authorKey, date, _worklogType);
		boolean isAutoCopy = false;
		Long wlogId = 0L;
		Long wlogIdExt = 0L;
		Issue issue = this.issueManager.getIssueObject(issueId);
		if (issue != null) {
			
		
			isAutoCopy = isWlAutoCopy(issue, worklogTypeId);
		
			IScnWorklog wlog =  scnDefaultWorklogService.createAndAutoAdjustRemainingEstimate(new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getUser()), worklog, true, isAutoCopy);
			
			
			wlogId = wlog.getId();
			Worklog ext = wlog.getLinkedWorklog();
			
			if (ext != null) {
				wlogIdExt = ext.getId();
			}
		}
		
		resultMap.put("wlId", wlogId);
		resultMap.put("wlIdExt", wlogIdExt);
		resultMap.put("isAuto", isAutoCopy);
		return resultMap;
	}
	
	public boolean createScnWorklog(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date,
			String worklogTypeId) throws DataAccessException {
		
		IScnWorklog worklog = formScnWorklog(issueId, _timeSpent, null, _comment, authorKey, date, _worklogType);
		boolean isAutoCopy = false;
		Issue issue = this.issueManager.getIssueObject(issueId);
		if (issue != null) {
			isAutoCopy = isWlAutoCopy(issue, worklogTypeId);			
			scnDefaultWorklogService.createAndAutoAdjustRemainingEstimate(new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getUser()), worklog, true, isAutoCopy); 
		}
		return isAutoCopy;
	}
	
	public IScnWorklog createScnWorklogWithoutCopy(Long issueId, String _worklogType, Long _timeSpent, String _comment, String authorKey, Date date,
			String worklogTypeId) throws DataAccessException {
		IScnWorklog newWorklog = null;
		
		IScnWorklog worklog = formScnWorklog(issueId, _timeSpent, null, _comment, authorKey, date, _worklogType);
		Issue issue = this.issueManager.getIssueObject(issueId);
		if (issue != null) {
			
			newWorklog = scnDefaultWorklogService.createAndAutoAdjustRemainingEstimate(new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getUser()), worklog, true, false);
		}
		return newWorklog;
	}
	
	public boolean updateScnWorklog(Long _worklogId, Worklog linkedWorklog) throws DataAccessException {
	
		IScnWorklog scnWorklog =  scnDefaultWorklogService.getById(new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getUser()), _worklogId);
		if (scnWorklog != null) {
			scnDefaultWorklogService.updateAndAutoAdjustRemainingEstimate(new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getUser()), updateWorklog(scnWorklog, linkedWorklog), true, false);
			
		}
		return true;
		
	}
	
	public IScnWorklog getScnWorklog(Long _worklogId) throws DataAccessException {
		return scnDefaultWorklogService.getById(new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getUser()), _worklogId);	
	}
	
	public boolean updateScnWorklog(Long _worklogId, String _worklogType, Long _timeSpent, String _comment) throws DataAccessException {
		
		IScnWorklog scnWorklog =  scnDefaultWorklogService.getById(new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getUser()), _worklogId);
				
		boolean isAutoCopy = isWlAutoCopyChecked(scnWorklog.getIssue(), scnWorklog.getWorklogTypeId(),scnWorklog);
				
		scnDefaultWorklogService.updateAndAutoAdjustRemainingEstimate(new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getUser()), updateWorklog(String.valueOf(_timeSpent), scnWorklog, _worklogType != null ? String.valueOf(_worklogType) : null, _comment), true, isAutoCopy);
		return isAutoCopy;
		
	}
	
	public boolean deleteScnWorklogById(Long worklogId) throws DataAccessException {
		
		IScnWorklog scnWorklog = getScnWorklog(worklogId);// ofBizScnWorklogStore.getById(worklogId);
		
		boolean isAutoCopy = isWlAutoCopyChecked(scnWorklog.getIssue(), scnWorklog.getWorklogTypeId(),scnWorklog);
		
		scnDefaultWorklogService.deleteAndAutoAdjustRemainingEstimate(new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getUser()), scnWorklog, true, isAutoCopy);
				
		return isAutoCopy;
		
	}
	
	private IScnWorklog updateWorklog(String timeSpend, IScnWorklog worklogOld, String _worklogType, String _comment) {
		IScnWorklog worklog = new ScnWorklogImpl(this.projectRoleManager, worklogOld.getIssue(), worklogOld.getId(), worklogOld.getAuthor(),
				_comment == null ? worklogOld.getComment() : _comment, worklogOld.getStartDate(), worklogOld.getGroupLevel(),
				worklogOld.getRoleLevelId(), Long.parseLong(timeSpend), worklogOld.getUpdateAuthor(), worklogOld.getCreated(),
				worklogOld.getUpdated(), _worklogType == null ? worklogOld.getWorklogTypeId() : _worklogType);
		worklog.setLinkedWorklog(worklogOld.getLinkedWorklog());
		return worklog;
	}
	
	private IScnWorklog updateWorklog(IScnWorklog worklogOld, Worklog linked) {
		IScnWorklog worklog = new ScnWorklogImpl(this.projectRoleManager, worklogOld.getIssue(), worklogOld.getId(), worklogOld.getAuthor(),
				worklogOld.getComment(), worklogOld.getStartDate(), worklogOld.getGroupLevel(), worklogOld.getRoleLevelId(),
				worklogOld.getTimeSpent(), worklogOld.getUpdateAuthor(), worklogOld.getCreated(), worklogOld.getUpdated(),
				worklogOld.getWorklogTypeId());
		worklog.setLinkedWorklog(linked);
		return worklog;
		
	}
	
	public IScnWorklog convertToWorklog(ProjectRoleManager projectRoleManager, GenericValue gv, Issue issue) {
		Timestamp startDateTS = gv.getTimestamp("startdate");
		Timestamp createdTS = gv.getTimestamp("created");
		Timestamp updatedTS = gv.getTimestamp("updated");
		String worklogType = (gv.getString("worklogtype") == null || gv.getString("worklogtype") == "") ? "0" : gv.getString("worklogtype");
		IScnWorklog worklog = new ScnWorklogImpl(projectRoleManager, issue, gv.getLong("id"), gv.getString("author"), gv.getString("body"),
				startDateTS != null ? new Date(startDateTS.getTime()) : null, gv.getString("grouplevel"), gv.getLong("rolelevel"),
				gv.getLong("timeworked"), gv.getString("updateauthor"), createdTS != null ? new Date(createdTS.getTime()) : null,
				updatedTS != null ? new Date(updatedTS.getTime()) : null, worklogType);
		
		Long linkedWorklogId = gv.getLong("linkedWorklog");
		worklog.setLinkedWorklog(linkedWorklogId == null ? null : worklogManager.getById(linkedWorklogId));
		
		return worklog;
	}
	
	public IScnWorklog formScnWorklog(Long issueId, Long _timeSpent, Long id, String _comment, String authorKey, Date date, String worklogTypeId) {
		Issue issue = this.issueManager.getIssueObject(issueId);
		IScnWorklog wl = new ScnWorklogImpl(projectRoleManager, issue, id, authorKey, _comment, date, null, null, _timeSpent, worklogTypeId);
		
		return wl;
	}
	
	public boolean isWlAutoCopy(Issue issue, String worklogTypeId) {
		
		// if user has no access to 'WL Auto Copy' checkbox, check project
		// settings
		// and then if worklog type doesn't specified or worklog should be
		// copied for the type, then return true
		
		return getWorklogAutoCopyOption(issue) && getWorklogTypeIsChecked(worklogTypeId, issue);
	}
		
	
	public boolean isWlAutoCopyChecked(Issue issue, String worklogTypeId, IScnWorklog worklog) {
		

    	if(new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getUser()).getErrorCollection().hasAnyErrors())
    		return isWlAutoCopy(issue,worklogTypeId);
    	
    	if (worklog!=null && worklog.getLinkedWorklog() == null){
    		return false;
    	} else {
    		return true;
    	}    	

	}
	
	public boolean getWorklogTypeIsChecked(String wlType, Issue issue) {
		System.out.println("wlType :" + wlType);
		if (StringUtils.isBlank(wlType) || (wlType != null && wlType.equals("0"))) {	
			return isUnspecifiedTypeAutoCopyEnabled(issue);
		}
		
		for (WorklogType type : getAutoCopyWorklogTypes(issue)) {		
			if (wlType.equals(type.getId()))
				return true;
		}
		
		return false;
	}
	
	public Collection<WorklogType> getAutoCopyWorklogTypes(Issue issue) {
		return projectSettignsManager.getWorklogTypes(issue.getProjectObject().getId());
	}
	
	public boolean isUnspecifiedTypeAutoCopyEnabled(Issue issue) {
		return projectSettignsManager.isUnspecifiedWLTypeAutoCopyEnabled(issue.getProjectObject().getId());
	}
	
	public boolean getWorklogAutoCopyOption(Issue issue) {
		return projectSettignsManager.isWLAutoCopyEnabled(issue.getProjectObject().getId());
	}
	
	public boolean isBlocked(User user, IScnWorklog wl) {
		return wl != null && (isProjectWLBlocked(getProjectId(wl), wl.getStartDate()) || isUserWLBlocked(user, wl.getStartDate()));
	}
	
	public boolean isProjectWLBlocked(Long projectId, Date date) {
		Date wlBlockingDate = this.projectSettignsManager.getWLBlockingDate(projectId);
		
		if (wlBlockingDate == null || date.after(wlBlockingDate)) {
			return false;
		}
		
		return true;
	}
	
	private Long getProjectId(Worklog wl) {
		return wl.getIssue().getProjectObject().getId();
	}
	
	protected boolean isUserWLBlocked(User user, Date date) {
		Date userBlockingDate = scnUserBlockingManager.getBlockingDate(user);
		
		if (userBlockingDate == null || date.after(userBlockingDate)) {
			return false;
		}
		return true;
	}
	
}