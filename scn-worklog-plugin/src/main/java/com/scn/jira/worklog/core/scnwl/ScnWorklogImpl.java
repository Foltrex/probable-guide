package com.scn.jira.worklog.core.scnwl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.JiraDateUtils;

import java.util.Date;

public class ScnWorklogImpl implements IScnWorklog {
	private final ProjectRoleManager projectRoleManager;
	private final Long id;
	private final String authorKey;
	private final String updateAuthorKey;
	private final String comment;
	private final String groupLevel;
	private final Long roleLevelId;
	private final Date created;
	private final Date updated;
	private final Date startDate;
	private final Long timeSpent;
	private final Issue issue;
	private String worklogTypeId;
	private Worklog linkedWorklog;

	public ScnWorklogImpl(ProjectRoleManager projectRoleManager, Issue issue, Long id, String authorKey, String comment,
			Date startDate, String groupLevel, Long roleLevelId, Long timeSpent, String worklogTypeId) {
		if (timeSpent == null) {
			throw new IllegalArgumentException("timeSpent must be set!");
		}
		this.projectRoleManager = projectRoleManager;
		this.authorKey = authorKey;
		this.updateAuthorKey = authorKey;
		this.comment = comment;
		this.groupLevel = groupLevel;
		this.roleLevelId = roleLevelId;
		this.timeSpent = timeSpent;
		Date createDate = new Date();
		this.startDate = ((startDate == null) ? createDate : startDate);
		this.created = createDate;
		this.updated = createDate;
		this.issue = issue;
		this.id = id;
		this.worklogTypeId = worklogTypeId;
	}

	public ScnWorklogImpl(ProjectRoleManager projectRoleManager, Issue issue, Long id, String authorKey, String comment,
			Date startDate, String groupLevel, Long roleLevelId, Long timeSpent, String updateAuthorKey, Date created,
			Date updated, String worklogTypeId) {
		if (timeSpent == null) {
			throw new IllegalArgumentException("timeSpent must be set!");
		}
		this.projectRoleManager = projectRoleManager;
		this.authorKey = authorKey;
		if (updateAuthorKey == null) {
			updateAuthorKey = this.authorKey;
		}
		this.updateAuthorKey = updateAuthorKey;
		this.comment = comment;
		this.groupLevel = groupLevel;
		this.roleLevelId = roleLevelId;
		this.timeSpent = timeSpent;
		Date createdDate = JiraDateUtils.copyOrCreateDateNullsafe(created);
		this.startDate = ((startDate == null) ? createdDate : startDate);
		this.created = createdDate;
		this.updated = ((updated == null) ? createdDate : updated);
		this.issue = issue;
		this.id = id;
		this.worklogTypeId = worklogTypeId;
	}

	public Long getId() {
		return this.id;
	}

	public String getAuthor() {
		return this.authorKey;
	}

	public String getAuthorFullName() {
		ApplicationUser user = getAuthorObject();
		if (user != null) {
			return user.getDisplayName();
		}
		return this.authorKey;
	}

	public String getUpdateAuthor() {
		return this.updateAuthorKey;
	}

	public String getUpdateAuthorFullName() {
		ApplicationUser user = getUpdateAuthorObject();
		if (user != null) {
			return user.getDisplayName();
		}
		return this.updateAuthorKey;
	}

	public String getAuthorKey() {
		return this.authorKey;
	}

	public ApplicationUser getAuthorObject() {
		return ApplicationUsers.byKey(this.authorKey);
	}

	public String getUpdateAuthorKey() {
		return this.updateAuthorKey;
	}

	public ApplicationUser getUpdateAuthorObject() {
		return ApplicationUsers.byKey(this.updateAuthorKey);
	}

	public Date getStartDate() {
		return JiraDateUtils.copyDateNullsafe(this.startDate);
	}

	public Long getTimeSpent() {
		return this.timeSpent;
	}

	public String getGroupLevel() {
		return this.groupLevel;
	}

	public Long getRoleLevelId() {
		return this.roleLevelId;
	}

	public ProjectRole getRoleLevel() {
		return (this.roleLevelId == null) ? null : projectRoleManager.getProjectRole(this.roleLevelId);
	}

	public String getComment() {
		return this.comment;
	}

	public Date getCreated() {
		return this.created;
	}

	public Date getUpdated() {
		return this.updated;
	}

	public Issue getIssue() {
		return this.issue;
	}

	public Worklog getLinkedWorklog() {
		return linkedWorklog;
	}

	public void setLinkedWorklog(Worklog linkedWorklog) {
		this.linkedWorklog = linkedWorklog;
	}

	public String getWorklogTypeId() {
		return this.worklogTypeId;
	}

	public void setWorklogTypeId(String worklogType) {
		this.worklogTypeId = worklogType;
	}
}
