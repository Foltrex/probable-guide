package com.scn.jira.automation.impl.domain.service;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.impl.domain.dto.IssueDto;
import com.scn.jira.automation.impl.domain.dto.ProjectDto;
import com.scn.jira.automation.impl.domain.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JiraContextServiceImpl implements JiraContextService {
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserManager userManager;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;

    @Autowired
    public JiraContextServiceImpl(JiraAuthenticationContext jiraAuthenticationContext,
                                  UserManager userManager, ProjectManager projectManager,
                                  IssueManager issueManager) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userManager = userManager;
        this.projectManager = projectManager;
        this.issueManager = issueManager;
    }

    @Override
    public ApplicationUser getCurrentUser() {
        return jiraAuthenticationContext.getLoggedInUser();
    }

    @Override
    public UserDto getUserDto() {
        ApplicationUser user = this.getCurrentUser();
        return new UserDto(user.getKey(), user.getDisplayName());
    }

    @Override
    public UserDto getUserDto(String userKey) {
        ApplicationUser user = userManager.getUserByKey(userKey);
        return user == null ? null : new UserDto(user.getKey(), user.getDisplayName());
    }

    @Override
    public ApplicationUser getUser(String userKey) {
        return userManager.getUserByKey(userKey);
    }

    @Override
    public ProjectDto getProjectDto(Long id) {
        Project project = projectManager.getProjectObj(id);
        return project == null ? null : new ProjectDto(project.getId(), project.getKey(), project.getName());
    }

    @Override
    public IssueDto getIssueDto(Long id) {
        MutableIssue issue = issueManager.getIssueObject(id);
        return issue == null ? null : new IssueDto(issue.getId(), issue.getKey(), issue.getSummary());
    }
}
