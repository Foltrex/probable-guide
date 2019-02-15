package com.scn.jira.worklog.globalsettings;

import java.util.List;
import com.atlassian.jira.user.ApplicationUser;

public interface IGlobalSettingsManager {
	public static final String SCN_TIMETRACKING = "scn_timetracking_permission";

	public List<String> getGroups();
	public void addGroups(List<String> groups);
	public void removeGroups(List<String> groups);
	public boolean hasPermission(String permission, ApplicationUser user);
}
