package com.scn.jira.worklog.globalsettings;

import java.util.List;

import com.atlassian.jira.user.ApplicationUser;

public interface IGlobalSettingsManager {
	String SCN_TIMETRACKING = "scn_timetracking_permission";

	List<String> getGroups();

	void addGroups(List<String> groups);

	void removeGroups(List<String> groups);

	boolean hasPermission(String permission, ApplicationUser user);
}
