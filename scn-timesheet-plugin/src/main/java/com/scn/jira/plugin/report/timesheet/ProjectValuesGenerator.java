package com.scn.jira.plugin.report.timesheet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.user.ApplicationUser;
import org.apache.commons.collections.map.ListOrderedMap;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.scn.jira.util.TextUtil;

@SuppressWarnings("rawtypes")
public class ProjectValuesGenerator implements ValuesGenerator {
	@Override
	public Map<String, String> getValues(Map params) {
		final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		Collection<Project> projectGVs = ComponentAccessor.getPermissionManager()
				.getProjects(ProjectPermissions.BROWSE_PROJECTS, user);
		@SuppressWarnings("unchecked")
		Map<String, String> projects = ListOrderedMap.decorate(new HashMap<String, String>(projectGVs.size()));

		projects.put("", "All Projects");
		for (Project project : projectGVs) {
			projects.put(project.getId().toString(), TextUtil.getUnquotedString(project.getName()));
		}

		return projects;
	}
}