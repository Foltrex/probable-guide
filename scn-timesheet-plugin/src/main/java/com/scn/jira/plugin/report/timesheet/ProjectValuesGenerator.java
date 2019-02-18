package com.scn.jira.plugin.report.timesheet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUsers;
import com.scn.jira.util.TextUtil;

public class ProjectValuesGenerator implements ValuesGenerator
{
	public Map<String, String> getValues(Map params)
	{
		User u = (User) params.get("User");
        Collection<Project> projectGVs = ComponentAccessor.getPermissionManager().getProjects(Permissions.BROWSE, ApplicationUsers.from(u));
        Map<String, String> projects = ListOrderedMap.decorate(new HashMap<String, String>(projectGVs.size()));
        
        projects.put("", "All Projects");
        for(Project project : projectGVs)
        {
          	projects.put(project.getId().toString(), TextUtil.getUnquotedString(project.getName()));
        }

		return projects;
	}
}