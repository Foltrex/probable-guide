package com.scn.jira.worklog.remote.service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssue;
import com.scn.jira.worklog.core.scnwl.IScnExtendedIssueStore;
import com.scn.jira.worklog.globalsettings.IGlobalSettingsManager;
import com.scn.jira.worklog.remote.service.object.RemoteScnExtIssue;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class RemoteScnExtIssueService implements IRemoteScnExtIssueService {
	private static final Logger LOGGER = Logger.getLogger(RemoteScnExtIssueService.class);

	private final PermissionManager permissionManager;
	private final IssueManager issueManager;
	private final IScnExtendedIssueStore scnExtendedIssueStore;
	private final IGlobalSettingsManager scnGlobalPermissionManager;

	@Inject
	public RemoteScnExtIssueService(@ComponentImport PermissionManager permissionManager, @ComponentImport IssueManager issueManager,
                IScnExtendedIssueStore scnExtendedIssueStore, IGlobalSettingsManager scnGlobalPermissionManager) {
		this.permissionManager = permissionManager;
		this.issueManager = issueManager;
		this.scnExtendedIssueStore = scnExtendedIssueStore;
		this.scnGlobalPermissionManager = scnGlobalPermissionManager;
	}

	public RemoteScnExtIssue getScnExtIssue(User user, String issueKey) throws RemoteException {
		if (StringUtils.isBlank(issueKey)) {
			return null;
		}
		final Issue issue = retrieveIssue(issueKey, user);
		IScnExtendedIssue scnIssue = null;
		if (issue != null) {
			scnIssue = this.scnExtendedIssueStore.getByIssue(issue);
		}
		if (scnIssue != null) {
			return RemoteScnExtIssue.convertToRemoteScnExtIssue(scnIssue);
		} else if (issue != null) {
			return RemoteScnExtIssue.convertToRemoteScnExtIssue(issue);
		} else {
			return null;
		}
	}

	public RemoteScnExtIssue[] getScnExtIssues(User user, List<String> issueKeys) throws RemoteException {
		if (issueKeys == null) {
			return null;
		}
		final List<RemoteScnExtIssue> result = new ArrayList<RemoteScnExtIssue>(issueKeys.size());
		for (String key : issueKeys) {
			try {
				final RemoteScnExtIssue remoteScnExtIssue = getScnExtIssue(user, key);
				if (remoteScnExtIssue != null) {
					result.add(remoteScnExtIssue);
				}
			} catch (RemoteException e) {
				LOGGER.warn(e.getMessage());
			}
		}
		return result.toArray(new RemoteScnExtIssue[result.size()]);
	}

	private Issue retrieveIssue(String issueKey, User user) throws RemoteException {
		final MutableIssue issue = issueManager.getIssueObject(issueKey);
		if (issue == null) {
			return null;
		}
		ApplicationUser appUser = ApplicationUsers.from(user);
		if (!this.permissionManager.hasPermission(Permissions.BROWSE, issue, appUser)
				|| !this.scnGlobalPermissionManager.hasPermission(IGlobalSettingsManager.SCN_TIMETRACKING, appUser)) {
			throw new RemoteException("You don't have permission to view the issue " + issueKey + ".");
		}
		return issue;
	}
}