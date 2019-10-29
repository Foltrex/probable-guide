package com.scn.jira.worklog.conditions;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.worklog.core.settings.IScnProjectSettingsManager;

public class ScnProjectSettingsCondition extends AbstractWebCondition {
	private final IScnProjectSettingsManager psManager;

	public ScnProjectSettingsCondition(IScnProjectSettingsManager psManager) {
		this.psManager = psManager;
	}

	@Override
	public boolean shouldDisplay(ApplicationUser user, JiraHelper jHelper) {
		Project project = jHelper.getProject();

		if (user == null || project == null)
			return false;

		return psManager.hasPermissionToViewWL(user, project);
	}
}
