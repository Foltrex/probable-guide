package com.scn.jira.worklog.globalsettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

@ExportAsService({GlobalSettingsManager.class })
@Named("globalSettingsManager")
public class GlobalSettingsManager implements IGlobalSettingsManager {
	// private static final Logger log = LoggerFactory.getLogger(ABC.class);
	private static final String PROPERTIES_SEPARATOR = ";";

	private final GroupManager groupManager;
	private final PropertiesManager propertiesManager;
	private final PermissionManager permissionManager;

	@Inject
	public GlobalSettingsManager(@ComponentImport final GroupManager groupManager,
			@ComponentImport final PermissionManager permissionManager) {
		this.groupManager = groupManager;
		this.permissionManager = permissionManager; // we need this to force spring scanner to index this dependency for webcondition
		this.propertiesManager = ComponentAccessor.getComponent(PropertiesManager.class);// this is the only way to resolve it
	}

	@Override
	public List<String> getGroups() {

		String value = propertiesManager.getPropertySet().getText(SCN_TIMETRACKING);

		List<String> groups = new ArrayList<String>();
		if (value != null) Collections.addAll(groups, value.split(PROPERTIES_SEPARATOR));
		return groups;
	}

	@Override
	public void addGroups(List<String> groupsToAdd) {

		List<String> groups = getGroups();
		groups.removeAll(groupsToAdd);
		groups.addAll(groupsToAdd);

		propertiesManager.getPropertySet().setText(SCN_TIMETRACKING, StringUtils.join(groups, PROPERTIES_SEPARATOR));
	}

	@Override
	public void removeGroups(List<String> groupsToRemove) {

		List<String> groups = getGroups();
		groups.removeAll(groupsToRemove);

		propertiesManager.getPropertySet().setText(SCN_TIMETRACKING, StringUtils.join(groups, PROPERTIES_SEPARATOR));
	}

	public boolean hasPermission(final String permission, ApplicationUser user) {
		assertionIsScnPermission(permission);
		Assertions.notNull("User", user);

		List<String> groups = getGroups();

		if (CollectionUtils.isEmpty(groups)) return false;

		Collection<String> userGroups = groupManager.getGroupNamesForUser(user);

		return CollectionUtils.containsAny(groups, userGroups);
	}

	protected void assertionIsScnPermission(String permission) {
		if (!SCN_TIMETRACKING.equals(permission)) throw new IllegalArgumentException(
				"PermissionType passed to this function must be a ScienceSoft global permission, " + permission + " is not");
	}
}
