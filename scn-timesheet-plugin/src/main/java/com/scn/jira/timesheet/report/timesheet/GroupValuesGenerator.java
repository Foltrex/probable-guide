package com.scn.jira.timesheet.report.timesheet;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.timesheet.util.TextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
public class GroupValuesGenerator implements ValuesGenerator<String> {
    private final GroupPickerSearchService groupPickerSearchService;
    private final GlobalPermissionManager globalPermissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    @Override
    public Map<String, String> getValues(Map params) {
        final ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
        Map<String, String> values = new TreeMap<>();
        if (globalPermissionManager.hasPermission(GlobalPermissionKey.USER_PICKER, user)) {
            Collection<Group> groups = groupPickerSearchService.findGroups("");
            for (Group group : groups)
                values.put(TextUtil.getUnquotedString(group.getName()), TextUtil.getUnquotedString(group.getName()));
        }

        return values;
    }
}
