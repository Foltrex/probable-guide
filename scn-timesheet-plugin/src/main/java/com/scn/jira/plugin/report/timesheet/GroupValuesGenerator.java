package com.scn.jira.plugin.report.timesheet;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.util.TextUtil;

@SuppressWarnings("rawtypes")
public class GroupValuesGenerator implements ValuesGenerator {
	@Override
	public Map<String, Object> getValues(Map params) {
		final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		Map<String, Object> values = new TreeMap<String, Object>();
		values.put("", "");
		if (ComponentAccessor.getGlobalPermissionManager().hasPermission(GlobalPermissionKey.USER_PICKER, user)) {
			Collection<Group> groups = ComponentAccessor.getComponent(GroupPickerSearchService.class).findGroups("");
			for (Group group : groups)
				values.put(TextUtil.getUnquotedString(group.getName()), TextUtil.getUnquotedString(group.getName()));
		}

		return values;
	}
}