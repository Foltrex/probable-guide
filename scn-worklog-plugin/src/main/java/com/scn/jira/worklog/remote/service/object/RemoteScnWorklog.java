package com.scn.jira.worklog.remote.service.object;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.atlassian.jira.issue.Issue;
import com.scn.jira.worklog.rest.util.JiraScnRestServiceUtil;

@XmlRootElement(name = "scn-worklog")
@XmlType(propOrder = { JiraScnRestServiceUtil.ISSUE_KEY_ATTR_NAME, JiraScnRestServiceUtil.ISSUE_ID_ATTR_NAME,
		JiraScnRestServiceUtil.AUTHOR_ATTR_NAME, JiraScnRestServiceUtil.UPDATE_AUTHOR_ATTR_NAME,
		JiraScnRestServiceUtil.COMMENT_ATTR_NAME, JiraScnRestServiceUtil.CREATED_ATTR_NAME,
		JiraScnRestServiceUtil.UPDATED_ATTR_NAME, JiraScnRestServiceUtil.START_DATE_ATTR_NAME,
		JiraScnRestServiceUtil.TIME_SPENT_ATTR_NAME, JiraScnRestServiceUtil.TIME_SPENT_SEC_ATTR_NAME,
		JiraScnRestServiceUtil.ID_ATTR_NAME, JiraScnRestServiceUtil.WORKLOG_TYPE_ATTR_NAME,
		JiraScnRestServiceUtil.GROUP_LEVEL_ATTR_NAME, JiraScnRestServiceUtil.ROLE_LEVEL_ID_ATTR_NAME })
public class RemoteScnWorklog {
	@XmlElement(name = JiraScnRestServiceUtil.ID_ATTR_NAME)
	private String id;

	@XmlElement(name = JiraScnRestServiceUtil.COMMENT_ATTR_NAME)
	private String comment;

	@XmlElement(name = JiraScnRestServiceUtil.GROUP_LEVEL_ATTR_NAME)
	private String groupLevel;

	@XmlElement(name = JiraScnRestServiceUtil.ROLE_LEVEL_ID_ATTR_NAME)
	private String roleLevelId;

	@XmlElement(name = JiraScnRestServiceUtil.START_DATE_ATTR_NAME)
	@XmlJavaTypeAdapter(value = com.scn.jira.worklog.rest.util.DateAdapter.class, type = Date.class)
	private Date startDate;

	@XmlElement(name = JiraScnRestServiceUtil.TIME_SPENT_ATTR_NAME)
	private String timeSpent;

	@XmlElement(name = JiraScnRestServiceUtil.AUTHOR_ATTR_NAME)
	private String author;

	@XmlElement(name = JiraScnRestServiceUtil.UPDATE_AUTHOR_ATTR_NAME)
	private String updateAuthor;

	@XmlElement(name = JiraScnRestServiceUtil.CREATED_ATTR_NAME)
	@XmlJavaTypeAdapter(value = com.scn.jira.worklog.rest.util.DateAdapter.class, type = Date.class)
	private Date created;

	@XmlElement(name = JiraScnRestServiceUtil.UPDATED_ATTR_NAME)
	@XmlJavaTypeAdapter(value = com.scn.jira.worklog.rest.util.DateAdapter.class, type = Date.class)
	private Date updated;

	@XmlElement(name = JiraScnRestServiceUtil.WORKLOG_TYPE_ATTR_NAME)
	private String worklogType;

	@XmlElement(name = JiraScnRestServiceUtil.TIME_SPENT_SEC_ATTR_NAME)
	private long timeSpentInSeconds;

	@XmlElement(name = JiraScnRestServiceUtil.ISSUE_KEY_ATTR_NAME)
	private String issueKey;

	@XmlElement(name = JiraScnRestServiceUtil.ISSUE_ID_ATTR_NAME)
	private Long issueId;

	public RemoteScnWorklog() {
	}

	public RemoteScnWorklog(String id, String comment, String groupLevel, String roleLevelId, Date startDate,
			String timeSpent, String author, String updateAuthor, Date created, Date updated, long timeSpentInSeconds,
			String worklogType, Issue issue) {
		this.id = id;
		this.comment = comment;
		this.groupLevel = groupLevel;
		this.roleLevelId = roleLevelId;
		this.startDate = startDate;
		this.timeSpent = timeSpent;
		this.author = author;
		this.updateAuthor = updateAuthor;
		this.created = created;
		this.updated = updated;
		this.timeSpentInSeconds = timeSpentInSeconds;
		this.worklogType = worklogType;
		this.issueKey = issue.getKey();
		this.issueId = issue.getId();
	}

	public String getId() {
		return id;
	}

	public String getComment() {
		return comment;
	}

	public String getGroupLevel() {
		return groupLevel;
	}

	public String getRoleLevelId() {
		return roleLevelId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public String getTimeSpent() {
		return timeSpent;
	}

	public String getAuthor() {
		return author;
	}

	public String getUpdateAuthor() {
		return updateAuthor;
	}

	public Date getCreated() {
		return created;
	}

	public Date getUpdated() {
		return updated;
	}

	public long getTimeSpentInSeconds() {
		return timeSpentInSeconds;
	}

	public String getWorklogType() {
		return worklogType;
	}

	public String getIssueKey() {
		return issueKey;
	}

	public Long getIssueId() {
		return issueId;
	}
}
