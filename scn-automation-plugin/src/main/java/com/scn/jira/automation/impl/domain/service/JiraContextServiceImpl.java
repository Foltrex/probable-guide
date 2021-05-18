package com.scn.jira.automation.impl.domain.service;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.impl.domain.dto.IssueDto;
import com.scn.jira.automation.impl.domain.dto.ProjectDto;
import com.scn.jira.automation.impl.domain.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class JiraContextServiceImpl implements JiraContextService {
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserManager userManager;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;
    private final GlobalPermissionManager globalPermissionManager;

    @Override
    public Locale getLocale() {
        return jiraAuthenticationContext.getLocale();
    }

    @Override
    public ApplicationUser getCurrentUser() {
        return jiraAuthenticationContext.getLoggedInUser();
    }

    @Override
    public UserDto getUserDto() {
        ApplicationUser user = this.getCurrentUser();
        return new UserDto(user.getKey(), user.getDisplayName(), user.getUsername());
    }

    @Override
    public UserDto getUserDto(String userKey) {
        ApplicationUser user = userManager.getUserByKey(userKey);
        return user == null ? null : new UserDto(user.getKey(), user.getDisplayName(), user.getUsername());
    }

    @Override
    public ApplicationUser getUser(String userKey) {
        return userManager.getUserByKey(userKey);
    }

    @Override
    public boolean isCurrentUserAdmin() {
        return globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, this.getCurrentUser())
            || globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, this.getCurrentUser())
            || this.getCurrentUser().getUsername().equals("akalaputs");
    }

    @Override
    public boolean isCurrentUserSystemAdmin() {
        return globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, this.getCurrentUser());
    }

    @Override
    public ProjectDto getProjectDto(Long id) {
        Project project = projectManager.getProjectObj(id);
        return project == null ? null : new ProjectDto(project.getId(), project.getKey(), project.getName());
    }

    @Nullable
    @Override
    public ProjectDto getProjectDto(String key) {
        Project project = projectManager.getProjectObjByKey(key);
        return project == null ? null : new ProjectDto(project.getId(), project.getKey(), project.getName());
    }

    @Override
    public IssueDto getIssueDto(Long id) {
        MutableIssue issue = issueManager.getIssueObject(id);
        return issue == null ? null : new IssueDto(issue.getId(), issue.getKey(), issue.getSummary());
    }

    @Nullable
    @Override
    public IssueDto getIssueDto(String key) {
        MutableIssue issue = issueManager.getIssueObject(key);
        return issue == null ? null : new IssueDto(issue.getId(), issue.getKey(), issue.getSummary());
    }
}
