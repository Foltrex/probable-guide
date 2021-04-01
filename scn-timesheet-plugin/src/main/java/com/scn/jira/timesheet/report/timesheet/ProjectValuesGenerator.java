package com.scn.jira.timesheet.report.timesheet;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.scn.jira.timesheet.util.TextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProjectValuesGenerator implements ValuesGenerator<String> {
    private final JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    private final PermissionManager permissionManager = ComponentAccessor.getPermissionManager();

    @Override
    public Map<String, String> getValues(Map params) {
        return permissionManager.getProjects(ProjectPermissions.BROWSE_PROJECTS, jiraAuthenticationContext.getLoggedInUser())
            .stream().collect(Collectors.toMap(
                project -> project.getId().toString(),
                project -> TextUtil.getUnquotedString(project.getName()),
                (oldValue, newValue) -> oldValue,
                ListOrderedMap::new
            ));
    }
}
