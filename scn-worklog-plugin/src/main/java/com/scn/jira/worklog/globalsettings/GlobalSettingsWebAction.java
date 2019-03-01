package com.scn.jira.worklog.globalsettings;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

public class GlobalSettingsWebAction extends JiraWebActionSupport {
	private final IGlobalSettingsManager wlGlobalSettingsManager;
	private final GroupManager groupManager;

	private List<String> availableGroups;
	private List<String> wlGroups;

    @Inject
	public GlobalSettingsWebAction(@ComponentImport final GroupManager groupManager,
								   final IGlobalSettingsManager wlGlobalSettingsManager) {
		this.groupManager = groupManager;
		this.wlGlobalSettingsManager = wlGlobalSettingsManager;
	}

	public List<String> getAvailableGroups() {
		if (this.availableGroups == null) {
			this.availableGroups = new ArrayList<String>(
					CollectionUtils.collect(
							groupManager.getAllGroups(),
							GlobalPermissionGroupAssociationUtil.GROUP_TO_GROUPNAME));
			this.availableGroups.removeAll(getWlGroups());
		}

		return this.availableGroups;
	}

	public List<String> getWlGroups() {
		if (this.wlGroups == null) {
			this.wlGroups = wlGlobalSettingsManager.getGroups();
		}

		return wlGroups;
	}

}
