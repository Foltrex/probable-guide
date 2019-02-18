package com.scn.jira.plugin.report.timesheet;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.util.TextUtil;

public class GroupValuesGenerator
		implements ValuesGenerator
{
	public Map<String, Object> getValues(Map params)
	{
		User u = (User) params.get("User");
		Map<String, Object> values = new TreeMap<String, Object>();
		values.put("", "");
		if (ComponentAccessor.getPermissionManager().hasPermission(Permissions.USER_PICKER, ApplicationUsers.from(u)))
		{
			UserManager userManager = ComponentAccessor.getUserManager();
			Collection<Group> groups = userManager.getGroups();
			for (Group group : groups)
			{
				values.put(TextUtil.getUnquotedString(group.getName()), TextUtil.getUnquotedString(group.getName()));
			}
			
		}
		
		return values;
	}
}