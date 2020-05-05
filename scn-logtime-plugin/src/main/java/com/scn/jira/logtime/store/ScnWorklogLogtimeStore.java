package com.scn.jira.logtime.store;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.*;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.ofbiz.core.entity.EntityOperator.*;

@Named
public class ScnWorklogLogtimeStore implements IScnWorklogLogtimeStore {
	public static final String SCN_WORKLOG_ISSUE_ENTITY = "ScnWorklogByIssueView";
    public static final String SCN_WORKLOG_PROJECT_ENTITY = "ScnWorklogByProjectView";

	private final IssueManager issueManager;
	private final ProjectRoleManager projectRoleManager;
	private final WorklogManager worklogManager;
	private final IScnProjectSettingsManager projectSettignsManager;
    private final IScnWorklogService scnDefaultWorklogService;

    @Inject
	public ScnWorklogLogtimeStore(IssueManager issueManager, ProjectRoleManager projectRoleManager,
                                  @Qualifier("overridedWorklogManager") WorklogManager worklogManager,
                                  IScnProjectSettingsManager projectSettignsManager,
                                  IScnWorklogService scnDefaultWorklogService) {
		this.issueManager = issueManager;
		this.projectRoleManager = projectRoleManager;
		this.worklogManager = worklogManager;
		this.projectSettignsManager = projectSettignsManager;
        this.scnDefaultWorklogService = scnDefaultWorklogService;
	}

	public List<IScnWorklog> getByProjectBetweenDates(boolean assignedCh, Project project, Date startDate, Date endDate,
			String user) throws DataAccessException {
		List<EntityCondition> conditions = Lists.newArrayList();
		endDate = DateUtils.getStartDate(1, endDate);
		conditions.add(new EntityExpr("startdate", LESS_THAN, new java.sql.Date(endDate.getTime())));
		conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new java.sql.Date(startDate.getTime())));
		conditions.add(new EntityExpr("projectId", EQUALS, project.getId()));
		conditions.add(new EntityExpr("author", EQUALS, user.trim()));
		EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);

		List<GenericValue> worklogGVs = ComponentAccessor.getOfBizDelegator()
				.findByCondition(SCN_WORKLOG_PROJECT_ENTITY, conditionList, null, ImmutableList.of("created ASC"));

		List<Long> issueIds = new ArrayList<>();
		for (GenericValue worklogGV : worklogGVs) {
			issueIds.add(worklogGV.getLong("issue"));
		}

		List<Issue> issueObjects = issueManager.getIssueObjects(issueIds);
		Map<Long, Issue> issueObjectsMap = new HashMap<>();

		for (Issue issueObject : issueObjects) {
			issueObjectsMap.put(issueObject.getId(), issueObject);
		}

		List<IScnWorklog> iScnWorklogs = new ArrayList<>();
		for (GenericValue worklogGV : worklogGVs) {
			Issue issue = issueObjectsMap.get(worklogGV.getLong("issue"));
			if (!assignedCh || issue != null && issue.getAssignee() != null && user.equals(issue.getAssignee().getName())) {
				iScnWorklogs.add(convertToWorklog(projectRoleManager, worklogGV, issue));
			}
		}

		return iScnWorklogs;
	}

	public List<IScnWorklog> getScnWorklogsByUserAndIssueBetweenDates(Issue issue, Date startDate, Date endDate,
			String user) throws DataAccessException {
		List<EntityCondition> conditions1 = new ArrayList<>();
		conditions1.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
		conditions1.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
		conditions1.add(new EntityExpr("issueId", EQUALS, issue.getId()));
		conditions1.add(new EntityExpr("author", EQUALS, user.trim()));
		EntityCondition conditionList = new EntityConditionList(conditions1, EntityOperator.AND);

		List<GenericValue> worklogGVs = ComponentAccessor.getOfBizDelegator().findByCondition(SCN_WORKLOG_ISSUE_ENTITY,
				conditionList, null, ImmutableList.of("created ASC"));

		List<IScnWorklog> iScnWorklogs = new ArrayList<>();
        for (GenericValue genericWorklog : worklogGVs) {
            IScnWorklog iScnWorklog = convertToWorklog(projectRoleManager, genericWorklog, issue);
            iScnWorklogs.add(iScnWorklog);
        }
		return iScnWorklogs;
	}

	public List<IScnWorklog> getScnWorklogsByIssueBetweenDates(Issue issue, Date startDate, Date endDate)
			throws DataAccessException {
		List<EntityCondition> conditions1 = new ArrayList<>();
		conditions1.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
		conditions1.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
		conditions1.add(new EntityExpr("issueId", EQUALS, issue.getId()));
		EntityCondition conditionList = new EntityConditionList(conditions1, EntityOperator.AND);

		List<GenericValue> worklogGVs = ComponentAccessor.getOfBizDelegator().findByCondition(SCN_WORKLOG_ISSUE_ENTITY,
				conditionList, null, ImmutableList.of("created ASC"));

		List<IScnWorklog> iScnWorklogs = new ArrayList<>();
        for (GenericValue genericWorklog : worklogGVs) {
            IScnWorklog iScnWorklog = convertToWorklog(projectRoleManager, genericWorklog, issue);
            iScnWorklogs.add(iScnWorklog);
        }
		return iScnWorklogs;
	}

	@Override
	public List<Long> getProjectIdsWithScnWorklogsBetweenDates(List<Long> projectIds, List<String> users,
			Date startDate, Date endDate) throws DataAccessException {
		List<EntityCondition> conditions = new ArrayList<>();
		conditions.add(new EntityExpr("startdate", GREATER_THAN_EQUAL_TO, new Timestamp(startDate.getTime())));
		conditions.add(new EntityExpr("startdate", LESS_THAN, new Timestamp(endDate.getTime())));
		conditions.add(new EntityExpr("projectId", IN, projectIds));
		conditions.add(new EntityExpr("author", IN, users));
		EntityCondition conditionList = new EntityConditionList(conditions, EntityOperator.AND);

		return ComponentAccessor.getOfBizDelegator()
				.findByCondition(SCN_WORKLOG_PROJECT_ENTITY, conditionList, ImmutableList.of("projectId")).stream()
				.map(x -> x.getLong("projectId")).distinct().collect(Collectors.toList());
	}

	public Map<String, Object> createScnWorklogResultMap(Long issueId, String _worklogType, Long _timeSpent,
			String _comment, String authorKey, Date date, String worklogTypeId) throws DataAccessException {

		Map<String, Object> resultMap = new HashMap<>();

		IScnWorklog worklog = formScnWorklog(issueId, _timeSpent, null, _comment, authorKey, date, _worklogType);
		boolean isAutoCopy = false;
		Long wlogId = 0L;
		Long wlogIdExt = 0L;
		Issue issue = this.issueManager.getIssueObject(issueId);
		if (issue != null) {
			isAutoCopy = isWlAutoCopy(issue, worklogTypeId);

			IScnWorklog wlog = scnDefaultWorklogService.createAndAutoAdjustRemainingEstimate(
					new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()),
					worklog, true, isAutoCopy);

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

	public boolean createScnWorklog(Long issueId, String _worklogType, Long _timeSpent, String _comment,
			String authorKey, Date date, String worklogTypeId) throws DataAccessException {

		IScnWorklog worklog = formScnWorklog(issueId, _timeSpent, null, _comment, authorKey, date, _worklogType);
		boolean isAutoCopy = false;
		Issue issue = this.issueManager.getIssueObject(issueId);
		if (issue != null) {
			isAutoCopy = isWlAutoCopy(issue, worklogTypeId);
			scnDefaultWorklogService.createAndAutoAdjustRemainingEstimate(
					new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()),
					worklog, true, isAutoCopy);
		}
		return isAutoCopy;
	}

	public IScnWorklog createScnWorklogWithoutCopy(Long issueId, String _worklogType, Long _timeSpent, String _comment,
			String authorKey, Date date, String worklogTypeId) throws DataAccessException {
		IScnWorklog newWorklog = null;

		IScnWorklog worklog = formScnWorklog(issueId, _timeSpent, null, _comment, authorKey, date, _worklogType);
		Issue issue = this.issueManager.getIssueObject(issueId);
		if (issue != null) {

			newWorklog = scnDefaultWorklogService.createAndAutoAdjustRemainingEstimate(
					new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()),
					worklog, true, false);
		}
		return newWorklog;
	}

	public boolean updateScnWorklog(Long _worklogId, Worklog linkedWorklog) throws DataAccessException {

		IScnWorklog scnWorklog = scnDefaultWorklogService.getById(
				new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()),
				_worklogId);
		if (scnWorklog != null) {
			scnDefaultWorklogService.updateAndAutoAdjustRemainingEstimate(
					new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()),
					updateWorklog(scnWorklog, linkedWorklog), true, false);

		}
		return true;

	}

	public boolean updateScnWorklogAndExt(Long _worklogId, Worklog linkedWorklog) throws DataAccessException {

		IScnWorklog scnWorklog = scnDefaultWorklogService.getById(
				new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()),
				_worklogId);
		if (scnWorklog != null && scnWorklog.getLinkedWorklog() != null) {
			IScnWorklog worklog = new ScnWorklogImpl(this.projectRoleManager, scnWorklog.getIssue(), scnWorklog.getId(),
					scnWorklog.getAuthorKey(), scnWorklog.getComment(), scnWorklog.getStartDate(),
					scnWorklog.getGroupLevel(), scnWorklog.getRoleLevelId(), scnWorklog.getTimeSpent(),
					scnWorklog.getUpdateAuthorKey(), scnWorklog.getCreated(), scnWorklog.getUpdated(),
					scnWorklog.getWorklogTypeId());
			worklog.setLinkedWorklog(linkedWorklog);

			scnDefaultWorklogService.updateAndAutoAdjustRemainingEstimate(
					new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()),
					worklog, true, true);
		}
		return true;
	}

	public IScnWorklog getScnWorklog(Long _worklogId) throws DataAccessException {
		return scnDefaultWorklogService.getById(
				new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()),
				_worklogId);
	}

	public boolean updateScnWorklog(Long _worklogId, String _worklogType, Long _timeSpent, String _comment)
			throws DataAccessException {

		IScnWorklog scnWorklog = scnDefaultWorklogService.getById(
				new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()),
				_worklogId);

		boolean isAutoCopy = isWlAutoCopyChecked(scnWorklog.getIssue(), scnWorklog.getWorklogTypeId(), scnWorklog);

		scnDefaultWorklogService
				.updateAndAutoAdjustRemainingEstimate(
						new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()),
						updateWorklog(String.valueOf(_timeSpent), scnWorklog,
                            _worklogType, _comment),
						true, isAutoCopy);
		return isAutoCopy;

	}

	public boolean deleteScnWorklogById(Long worklogId) throws DataAccessException {

		IScnWorklog scnWorklog = getScnWorklog(worklogId);

		boolean isAutoCopy = isWlAutoCopyChecked(scnWorklog.getIssue(), scnWorklog.getWorklogTypeId(), scnWorklog);

		scnDefaultWorklogService.deleteAndAutoAdjustRemainingEstimate(
				new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()),
				scnWorklog, true, isAutoCopy);

		return isAutoCopy;
	}

	private IScnWorklog updateWorklog(String timeSpend, IScnWorklog worklogOld, String _worklogType, String _comment) {
		IScnWorklog worklog = new ScnWorklogImpl(this.projectRoleManager, worklogOld.getIssue(), worklogOld.getId(),
				worklogOld.getAuthorKey(), _comment == null ? worklogOld.getComment() : _comment,
				worklogOld.getStartDate(), worklogOld.getGroupLevel(), worklogOld.getRoleLevelId(),
				Long.parseLong(timeSpend), worklogOld.getUpdateAuthorKey(), worklogOld.getCreated(),
				worklogOld.getUpdated(), _worklogType == null ? worklogOld.getWorklogTypeId() : _worklogType);
		worklog.setLinkedWorklog(worklogOld.getLinkedWorklog());
		return worklog;
	}

	private IScnWorklog updateWorklog(IScnWorklog worklogOld, Worklog linked) {
		IScnWorklog worklog = new ScnWorklogImpl(this.projectRoleManager, worklogOld.getIssue(), worklogOld.getId(),
				worklogOld.getAuthorKey(), worklogOld.getComment(), worklogOld.getStartDate(),
				worklogOld.getGroupLevel(), worklogOld.getRoleLevelId(), worklogOld.getTimeSpent(),
				worklogOld.getUpdateAuthorKey(), worklogOld.getCreated(), worklogOld.getUpdated(),
				worklogOld.getWorklogTypeId());
		worklog.setLinkedWorklog(linked);
		return worklog;

	}

	public IScnWorklog convertToWorklog(ProjectRoleManager projectRoleManager, GenericValue gv, Issue issue) {
		Timestamp startDateTS = gv.getTimestamp("startdate");
		Timestamp createdTS = gv.getTimestamp("created");
		Timestamp updatedTS = gv.getTimestamp("updated");
		String worklogType = (gv.getString("worklogtype") == null || gv.getString("worklogtype").equals("")) ? "0"
				: gv.getString("worklogtype");
		IScnWorklog worklog = new ScnWorklogImpl(projectRoleManager, issue, gv.getLong("id"), gv.getString("author"),
				gv.getString("body"), startDateTS != null ? new Date(startDateTS.getTime()) : null,
				gv.getString("grouplevel"), gv.getLong("rolelevel"), gv.getLong("timeworked"),
				gv.getString("updateauthor"), createdTS != null ? new Date(createdTS.getTime()) : null,
				updatedTS != null ? new Date(updatedTS.getTime()) : null, worklogType);

		Long linkedWorklogId = gv.getLong("linkedWorklog");
		worklog.setLinkedWorklog(linkedWorklogId == null ? null : worklogManager.getById(linkedWorklogId));

		return worklog;
	}

	public IScnWorklog formScnWorklog(Long issueId, Long _timeSpent, Long id, String _comment, String authorKey,
			Date date, String worklogTypeId) {
		Issue issue = this.issueManager.getIssueObject(issueId);

        return new ScnWorklogImpl(projectRoleManager, issue, id, authorKey, _comment, date, null, null,
                _timeSpent, worklogTypeId);
	}

	public boolean isWlAutoCopy(Issue issue, String worklogTypeId) {
		// if user has no access to 'WL Auto Copy' checkbox, check project
		// settings
		// and then if worklog type doesn't specified or worklog should be
		// copied for the type, then return true

		return getWorklogAutoCopyOption(issue) && getWorklogTypeIsChecked(worklogTypeId, issue);
	}

	public boolean isWlAutoCopyChecked(Issue issue, String worklogTypeId, IScnWorklog worklog) {
		if (new JiraServiceContextImpl(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser())
				.getErrorCollection().hasAnyErrors())
			return isWlAutoCopy(issue, worklogTypeId);

        return worklog == null || worklog.getLinkedWorklog() != null;
	}

	public boolean getWorklogTypeIsChecked(String wlType, Issue issue) {
		if (StringUtils.isBlank(wlType) || wlType.equals("0")) {
			return isUnspecifiedTypeAutoCopyEnabled(issue);
		}

		for (WorklogType type : getAutoCopyWorklogTypes(issue)) {
			if (wlType.equals(type.getId()))
				return true;
		}

		return false;
	}

	public Collection<WorklogType> getAutoCopyWorklogTypes(Issue issue) {
		return projectSettignsManager.getWorklogTypes(Objects.requireNonNull(issue.getProjectObject()).getId());
	}

	public boolean isUnspecifiedTypeAutoCopyEnabled(Issue issue) {
		return projectSettignsManager.isUnspecifiedWLTypeAutoCopyEnabled(Objects.requireNonNull(issue.getProjectObject()).getId());
	}

	public boolean getWorklogAutoCopyOption(Issue issue) {
		return projectSettignsManager.isWLAutoCopyEnabled(Objects.requireNonNull(issue.getProjectObject()).getId());
	}

	public boolean isProjectWLBlocked(Long projectId, Date date) {
		Date wlBlockingDate = this.projectSettignsManager.getWLBlockingDate(projectId);

        return wlBlockingDate != null && !date.after(wlBlockingDate);
    }

	public boolean isProjectWLWorklogBlocked(Long projectId, Date date) {
		Date wlWorklogBlockingDate = this.projectSettignsManager.getWLWorklogBlockingDate(projectId);

        return wlWorklogBlockingDate != null && !date.after(wlWorklogBlockingDate);
    }
}
