package com.scn.jira.worklog.globalsettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.PropertiesManager;
import org.apache.commons.collections.CollectionUtils;

import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

public class GlobalSettingsWebAction extends JiraWebActionSupport {
	private final GroupManager groupManager;

	private List<String> availableGroups;
	private List<String> wlGroups;

    @Inject
	public GlobalSettingsWebAction(@ComponentImport final GroupManager groupManager) {
		this.groupManager = groupManager;
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
			PropertiesManager component = ComponentAccessor.getComponent(PropertiesManager.class);
			String value = component.getPropertySet().getText(IGlobalSettingsManager.SCN_TIMETRACKING);

			List<String> groups = new ArrayList<String>();

			if (value != null) Collections.addAll(groups, value.split(";"));

			this.wlGroups = groups;
		}

		return wlGroups;
	}

}
