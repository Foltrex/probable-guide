package com.scn.jira.worklog.globalsettings;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Named
public class GlobalSettingsManager implements IGlobalSettingsManager {
    private static final String PROPERTIES_SEPARATOR = ";";

    private final GroupManager groupManager;
    private final PropertiesManager propertiesManager;

    @Inject
    public GlobalSettingsManager(final GroupManager groupManager) {
        this.groupManager = groupManager;
        this.propertiesManager = ComponentAccessor.getComponent(PropertiesManager.class);// this is the only way to resolve it
    }

    @Override
    public List<String> getGroups() {

        String value = propertiesManager.getPropertySet().getText(SCN_TIMETRACKING);

        List<String> groups = new ArrayList<>();
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

    @Override
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
