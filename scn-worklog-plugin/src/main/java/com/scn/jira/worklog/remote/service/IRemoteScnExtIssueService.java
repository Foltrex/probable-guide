package com.scn.jira.worklog.remote.service;

import java.rmi.RemoteException;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.scn.jira.worklog.remote.service.object.RemoteScnExtIssue;

public interface IRemoteScnExtIssueService {
	public RemoteScnExtIssue getScnExtIssue(User user, String issueKey) throws RemoteException;

	public RemoteScnExtIssue[] getScnExtIssues(User user, List<String> issueKeys) throws RemoteException;
}
