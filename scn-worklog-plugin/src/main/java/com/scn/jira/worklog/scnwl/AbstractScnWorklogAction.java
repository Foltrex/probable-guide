package com.scn.jira.worklog.scnwl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;
import com.atlassian.jira.web.action.util.CalendarResourceIncluder;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.scnwl.ScnWorklogImpl;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;

public abstract class AbstractScnWorklogAction extends AbstractIssueSelectAction {

	private static final long serialVersionUID = -7430574077126963463L;
	protected static final String ADJUST_ESTIMATE_AUTO = "auto";
	protected static final String ADJUST_ESTIMATE_NEW = "new";
	protected static final String ADJUST_ESTIMATE_MANUAL = "manual";

	protected final CommentService commentService;
	protected final ProjectRoleManager projectRoleManager;
	protected final JiraDurationUtils jiraDurationUtils;
	protected final GroupManager groupManager;
	protected final IScnExtendedIssueStore extIssueStore;
	protected final IScnWorklogService scnWorklogService;
	protected final IScnProjectSettingsManager projectSettignsManager;
	protected final ExtendedConstantsManager extendedConstantsManager;

	private String worklogType;
	private Long worklogId;
	private boolean wlAutoCopy = false;
	private String timeLogged;
	private String inputReporter;
	private String startDate;
	private String workType;
	private String newEstimate;
	private String adjustmentAmount;
	private String commentLevel;
	private String comment;
	protected String adjustEstimate;
	private CommentVisibility commentVisibility;
	final CalendarResourceIncluder calendarResourceIncluder = new CalendarResourceIncluder();

	public AbstractScnWorklogAction(CommentService commentService, ProjectRoleManager projectRoleManager,
			JiraDurationUtils jiraDurationUtils, GroupManager groupManager, IScnExtendedIssueStore extIssueStore,
			IScnWorklogService worklogService,
			IScnProjectSettingsManager projectSettignsManager, ExtendedConstantsManager extendedConstantsManager) {
		adjustEstimate = "auto";
		this.commentService = commentService;
		this.projectRoleManager = projectRoleManager;
		this.jiraDurationUtils = jiraDurationUtils;
		this.groupManager = groupManager;
		this.extIssueStore = extIssueStore;
		this.scnWorklogService = worklogService;
		this.projectSettignsManager = projectSettignsManager;
		this.extendedConstantsManager = extendedConstantsManager;
	}

	public boolean shouldDisplay() {
		return isIssueValid() /*&& hasIssuePermission("work", getIssueObject())*/
				&& this.scnWorklogService.hasPermissionToView(getJiraServiceContext(), getIssueObject())
				&& isWorkflowAllowsEdit(getIssueObject());
	}

	public String getModifierKey() {
		return BrowserUtils.getModifierKey();
	}

	public boolean getHasCalendarTranslation() {
		return calendarResourceIncluder.hasTranslation(getJiraServiceContext().getI18nBean().getLocale());
	}

	public Calendar getCurrentCalendar() {
		return Calendar.getInstance(getJiraServiceContext().getI18nBean().getLocale());
	}

	public CalendarResourceIncluder getCalendarIncluder() {
		return calendarResourceIncluder;
	}

	public long getCurrentTimeMillis() {
		return System.currentTimeMillis();
	}

	public String getComment() {
		return comment;
	}

	public String getEstimate() throws Exception {
		final IScnExtendedIssue extIssue = extIssueStore.getByIssue(getIssueObject());
		Long estimate = (extIssue == null) ? getIssueObject().getOriginalEstimate() : extIssue.getEstimate();
		return estimate != null ? jiraDurationUtils.getFormattedDuration(estimate) : null;
	}

	public String getTimeLogged() {
		return timeLogged;
	}

	public void setTimeLogged(String timeLogged) {
		this.timeLogged = timeLogged;
	}

	public String getWorkType() {
		return workType;
	}

	public void setWorkType(String workType) {
		this.workType = workType;
	}

	public String getNewEstimate() {
		return newEstimate;
	}

	public void setNewEstimate(String newEstimate) {
		this.newEstimate = newEstimate;
	}

	public String getAdjustmentAmount() {
		return adjustmentAmount;
	}

	public void setAdjustmentAmount(String adjustmentAmount) {
		this.adjustmentAmount = adjustmentAmount;
	}

	public String getAdjustEstimate() {
		return adjustEstimate;
	}

	public void setAdjustEstimate(String adjustEstimate) {
		this.adjustEstimate = adjustEstimate;
	}

	public boolean isLevelSelected(String visibilityLevel) {
		return getCommentLevel() != null && getCommentLevel().equals(visibilityLevel);
	}

	public String getCommentLevel() {
		return commentLevel;
	}

	public void setCommentLevel(String commentLevel) {
		this.commentLevel = commentLevel;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Collection<Group> getGroupLevels() {
		Collection<Group> groups;
		if (getLoggedInUser() == null || !commentService.isGroupVisibilityEnabled()) groups = Collections.emptyList();
		else groups = groupManager.getGroupsForUser(getLoggedInUser());
		return groups;
	}

	public Collection<ProjectRole> getRoleLevels() {
		Collection<ProjectRole> roleLevels;
		if (commentService.isProjectRoleVisibilityEnabled()) roleLevels = projectRoleManager.getProjectRoles(
				getLoggedInUser(), getIssueObject().getProjectObject());
		else roleLevels = Collections.emptyList();
		return roleLevels;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	protected CommentVisibility getCommentVisibility() {
		if (commentVisibility == null) commentVisibility = new CommentVisibility(commentLevel);
		return commentVisibility;
	}

	protected Date getParsedStartDate() {
		try {
			return getStartDate() != null ? getDateTimeFormatter().withStyle(DateTimeStyle.COMPLETE).parse(getStartDate()) : null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}

	protected String getFormattedStartDate(Date date) {
		return getDateTimeFormatter().withStyle(DateTimeStyle.COMPLETE).format(date);
	}

	public Long getWorklogId() {
		return worklogId;
	}

	public void setWorklogId(Long worklogId) {
		this.worklogId = worklogId;
	}

	public boolean isWlAutoCopy() {
		// if user has access to 'WL Auto Copy' checkbox, return its value
		if (hasPermissionToViewWL()) {
			return wlAutoCopy;
		}
		// if user has no access to 'WL Auto Copy' checkbox, check project settings
		// and then if worklog type doesn't specified or worklog should be copied for the type, then return true
		return getWorklogAutoCopyOption() && getWorklogTypeIsChecked(getWorklogType());
	}

	public boolean isWlAutoCopyChecked() {
		if (getJiraServiceContext().getErrorCollection().hasAnyErrors()) return isWlAutoCopy();

		if (getWorklogAutoCopyOption()) return getWorklogTypeIsChecked(getWorklogType());

		return false;
	}

	public boolean getWorklogTypeIsChecked(String wlType) {
		if (StringUtils.isBlank(wlType)) return isUnspecifiedTypeAutoCopyEnabled();

		for (WorklogType type : getAutoCopyWorklogTypes()) {
			if (wlType.equals(type.getId())) return true;
		}

		return false;
	}

	public void setWlAutoCopy(boolean wlAutoCopy) {
		this.wlAutoCopy = wlAutoCopy;
	}

	public String getWorklogType() {
		if (this.worklogType == null) {
			return "";
		}
		return this.worklogType;
	}

	public void setWorklogType(String worklogType) {
		this.worklogType = worklogType;
	}

	public Collection<WorklogType> getWorklogTypeObjects() {
		return this.extendedConstantsManager.getWorklogTypeObjects();
	}

	public boolean isWorklogTypeSelected(String worklogType) {
		return (getWorklogType() != null) && (getWorklogType().equals(worklogType));
	}

	public ProjectManager getProjectManager() {
		return ComponentAccessor.getProjectManager();
	}

	public Map<String, String> getAssignableUsers() {
		try {
			List<ApplicationUser> users = new ArrayList<ApplicationUser>();

			users.addAll(ComponentAccessor.getPermissionSchemeManager().getUsers(new Long(Permissions.WORK_ISSUE),
					ComponentAccessor.getPermissionContextFactory().getPermissionContext(getIssueObject())));

			if (CollectionUtils.isEmpty(users)) return Collections.emptyMap();

			Collections.sort(users, new UserBestNameComparator(getJiraServiceContext().getI18nBean().getLocale()));

			final Map<String, String> assignableUsers = new ListOrderedMap();
			for (ApplicationUser user : users) {
				if (scnWorklogService.hasPermissionToCreate(getJiraServiceContext(), getIssueObject())) {
					assignableUsers.put(user.getName(), user.getDisplayName());
				}
			}
			return assignableUsers;
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyMap();
		}
	}

	protected IScnWorklog reassignWorklog(IScnWorklog worklog, ApplicationUser reporter) {
		assert worklog != null;
		assert reporter != null;

		final IScnWorklog reassignedWorklog = new ScnWorklogImpl(this.projectRoleManager, worklog.getIssue(), worklog.getId(),
				reporter.getKey(), worklog.getComment(), worklog.getStartDate(), worklog.getGroupLevel(),
				worklog.getRoleLevelId(), worklog.getTimeSpent(), getLoggedInUser().getKey(), worklog.getCreated(),
				worklog.getUpdated(), worklog.getWorklogTypeId());
		reassignedWorklog.setLinkedWorklog(worklog.getLinkedWorklog());
		return reassignedWorklog;
	}

	public String getInputReporter() {
		return inputReporter;
	}

	public void setInputReporter(String inputReporter) {
		this.inputReporter = inputReporter;
	}

	public boolean getWorklogAutoCopyOption() {
		return projectSettignsManager.isWLAutoCopyEnabled(getIssueObject().getProjectObject().getId());
	}

	public boolean hasPermissionToViewWL() {
		return projectSettignsManager.hasPermissionToViewWL(getLoggedInUser(), getIssueObject().getProjectObject());
	}

	public Collection<WorklogType> getAutoCopyWorklogTypes() {
		return projectSettignsManager.getWorklogTypes(getIssueObject().getProjectObject().getId());
	}

	public boolean isUnspecifiedTypeAutoCopyEnabled() {
		return projectSettignsManager.isUnspecifiedWLTypeAutoCopyEnabled(getIssueObject().getProjectObject().getId());
	}

	public boolean isWlTypeRequired() {
		return projectSettignsManager.isWLTypeRequired(getIssueObject().getProjectObject().getId());
	}
}
