package com.scn.jira.worklog.wl;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.worklog.DefaultWorklogManager;
import com.atlassian.jira.issue.worklog.TimeTrackingIssueUpdater;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogStore;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.scn.jira.worklog.core.scnwl.IScnWorklogStore;

import javax.inject.Inject;
import javax.inject.Named;

@ExportAsService({OverridedWorklogManager.class })
@Named("overridedWorklogManager")
public class OverridedWorklogManager extends DefaultWorklogManager {
	private final IScnWorklogStore scnWorklogStore;
	private final TimeTrackingIssueUpdater timeTrackingIssueUpdater;

	@Inject
	public OverridedWorklogManager(ProjectRoleManager projectRoleManager,
			TimeTrackingIssueUpdater timeTrackingIssueUpdater, IScnWorklogStore scnWorklogStore, @ComponentImport EventPublisher eventPublisher) {
		super(projectRoleManager, ComponentAccessor.getComponent(WorklogStore.class), timeTrackingIssueUpdater, eventPublisher);

		this.timeTrackingIssueUpdater = timeTrackingIssueUpdater;
		this.scnWorklogStore = scnWorklogStore;
	}

	public boolean delete(ApplicationUser user, Worklog worklog, Long newEstimate, boolean dispatchEvent) {
		validateWorklog(worklog, false);
		this.timeTrackingIssueUpdater.updateIssueOnWorklogDelete(user, worklog, newEstimate, dispatchEvent);
		return scnWorklogStore.deleteLinkedWorklog(worklog.getId());
	}

	void validateWorklog(Worklog worklog, boolean create) {
		if (worklog == null) throw new IllegalArgumentException("Worklog must not be null.");
		if (worklog.getIssue() == null) throw new IllegalArgumentException("The worklogs issue must not be null.");
		if ((!create) && (worklog.getId() == null)) throw new IllegalArgumentException("Can not modify a worklog with a null id.");
	}
}