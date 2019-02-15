package com.scn.jira.worklog.remote.service.object;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.atlassian.jira.issue.Issue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.rest.util.JiraScnRestServiceUtil;

@XmlRootElement(name = "scn-extended-issue")
@XmlType(propOrder = { JiraScnRestServiceUtil.ISSUE_KEY_ATTR_NAME, JiraScnRestServiceUtil.TIMESPENT_ATTR_NAME,
		JiraScnRestServiceUtil.ORIGINAL_EST_ATTR_NAME, JiraScnRestServiceUtil.ESTIMATE_LEVEL_ATTR_NAME })
public class RemoteScnExtIssue {

	@XmlElement(name = JiraScnRestServiceUtil.ISSUE_KEY_ATTR_NAME)
	private String issueKey;

	@XmlElement(name = JiraScnRestServiceUtil.ORIGINAL_EST_ATTR_NAME)
	private Long originalEstimate;

	@XmlElement(name = JiraScnRestServiceUtil.ESTIMATE_LEVEL_ATTR_NAME)
	private Long estimate;

	@XmlElement(name = JiraScnRestServiceUtil.TIMESPENT_ATTR_NAME)
	private Long timeSpent;

	public static RemoteScnExtIssue convertToRemoteScnExtIssue(Issue issue) {
		if (issue == null) {
			return null;
		}
		final RemoteScnExtIssue remoteScnExtIssue = new RemoteScnExtIssue(issue.getKey(), null, null, null);
		return remoteScnExtIssue;
	}

	public static RemoteScnExtIssue convertToRemoteScnExtIssue(IScnExtendedIssue scnExtIssue) {
		if (scnExtIssue == null) {
			return null;
		}
		final RemoteScnExtIssue remoteScnExtIssue = new RemoteScnExtIssue(scnExtIssue.getIssue().getKey(),
				scnExtIssue.getOriginalEstimate(), scnExtIssue.getEstimate(), scnExtIssue.getTimeSpent());
		return remoteScnExtIssue;
	}

	public RemoteScnExtIssue() {
	}

	public RemoteScnExtIssue(String issueKey, Long originalEstimate, Long estimate, Long timeSpent) {
		this.issueKey = issueKey;
		this.originalEstimate = originalEstimate;
		this.estimate = estimate;
		this.timeSpent = timeSpent;
	}

	public String getIssueKey() {
		return issueKey;
	}

	public Long getOriginalEstimate() {
		return originalEstimate;
	}

	public Long getEstimate() {
		return estimate;
	}

	public Long getTimeSpent() {
		return timeSpent;
	}
}
