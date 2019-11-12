package com.scn.jira.worklog.remote.service;

import java.rmi.RemoteException;
import java.util.List;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.lang.StringUtils;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.scn.jira.worklog.core.scnwl.IScnWorklog;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import com.scn.jira.worklog.remote.service.object.RemoteScnWorklog;
import com.scn.jira.worklog.scnwl.IScnWorklogService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class RemoteScnWorklogService implements IRemoteScnWorklogService {
	// private static final org.apache.log4j.Logger LOGGER =
	// org.apache.log4j.Logger.getLogger(RemoteScnWorklogService.class);

	private final IssueManager issueManager;
	private final ProjectManager projectManager;
	private final JiraAuthenticationContext authenticationContext;
	private final JiraDurationUtils jiraDurationUtils;
	private final IScnWorklogService scnWorklogService;
	private final ExtendedConstantsManager extendedConstantsManager;

	@Inject
	public RemoteScnWorklogService(@ComponentImport IssueManager issueManager,
			@ComponentImport ProjectManager projectManager,
			@ComponentImport JiraAuthenticationContext authenticationContext,
			IScnWorklogService defaultScnWorklogService, ExtendedConstantsManager extendedConstantsManager) {
		this.issueManager = issueManager;
		this.projectManager = projectManager;
		this.authenticationContext = authenticationContext;
		this.jiraDurationUtils = ComponentAccessor.getComponent(JiraDurationUtils.class);
		this.scnWorklogService = defaultScnWorklogService;
		this.extendedConstantsManager = extendedConstantsManager;
	}

	public RemoteScnWorklog[] getScnWorklogs(User user, String issueKey) throws RemoteException {
		final JiraServiceContext serviceContext = new JiraServiceContextImpl(ApplicationUsers.from(user));
		final Issue issue = getIssueByKey(issueKey);
		final List<IScnWorklog> scnWorklogs = this.scnWorklogService.getByIssueVisibleToUser(serviceContext, issue);
		final RemoteScnWorklog[] remoteScnWorklogs = new RemoteScnWorklog[scnWorklogs.size()];
		int i = 0;
		for (final IScnWorklog scnWorklog : scnWorklogs) {
			final RemoteScnWorklog remoteScnWorklog = convertToRemoteScnWorkLog(scnWorklog, issueKey);
			remoteScnWorklogs[i] = remoteScnWorklog;
			i++;
		}
		return remoteScnWorklogs;
	}

	private RemoteScnWorklog convertToRemoteScnWorkLog(IScnWorklog scnWorklog, String issueKey) {
		if (scnWorklog == null) {
			return null;
		}

		String id = scnWorklog.getId() == null ? null : String.valueOf(scnWorklog.getId());
		String roleLevelId = scnWorklog.getRoleLevelId() == null ? null : String.valueOf(scnWorklog.getTimeSpent());
		long timeSpentInSeconds = scnWorklog.getTimeSpent().longValue();
		String timeSpentDuration = this.jiraDurationUtils.getFormattedDuration(new Long(timeSpentInSeconds));
		String worklogType = getWorklogType(scnWorklog.getWorklogTypeId());

		final RemoteScnWorklog remoteScnWorklog = new RemoteScnWorklog(id, scnWorklog.getComment(),
				scnWorklog.getGroupLevel(), roleLevelId, scnWorklog.getStartDate(), timeSpentDuration,
				scnWorklog.getAuthorKey(), scnWorklog.getUpdateAuthorKey(), scnWorklog.getCreated(),
				scnWorklog.getUpdated(), timeSpentInSeconds, worklogType, issueKey);

		return remoteScnWorklog;
	}

	private String getWorklogType(String worklogTypeId) {
		if (StringUtils.isBlank(worklogTypeId)) {
			return null;
		}
		WorklogType type = this.extendedConstantsManager.getWorklogTypeObject(worklogTypeId);
		if (type == null) {
			return null;
		} else {
			return type.getName();
		}
	}

	protected Project getProjectByKey(String projectKey) throws RemoteException {
		Project project = null;
		try {
			project = projectManager.getProjectObjByKey(projectKey);
			if (project == null) {
				throw new RemoteException(getI18nHelper().getText("issue.does.not.exist.desc", projectKey));
			}
		} catch (DataAccessException e) {
			throw new RemoteException();
		} catch (RuntimeException e) {
			throw new RemoteException();
		}
		return project;
	}

	protected Issue getIssueByKey(String issueKey) throws RemoteException {
		Issue issue;
		try {
			issue = issueManager.getIssueObject(issueKey);
			if (issue == null) {
				throw new RemoteException(getI18nHelper().getText("issue.does.not.exist.desc", issueKey));
			}
		} catch (DataAccessException dae) {
			throw new RemoteException();
		} catch (RuntimeException rt) {
			throw new RemoteException();
		}
		return issue;
	}

	protected I18nHelper getI18nHelper() {
		return authenticationContext.getI18nHelper();
	}
}