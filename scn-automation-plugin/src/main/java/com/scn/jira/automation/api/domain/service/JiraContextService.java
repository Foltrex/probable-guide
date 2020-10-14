package com.scn.jira.automation.api.domain.service;

import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.automation.impl.domain.dto.IssueDto;
import com.scn.jira.automation.impl.domain.dto.ProjectDto;
import com.scn.jira.automation.impl.domain.dto.UserDto;

import javax.annotation.Nullable;
import java.util.Locale;

public interface JiraContextService {
    Locale getLocale();

    ApplicationUser getCurrentUser();

    UserDto getUserDto();

    @Nullable
    UserDto getUserDto(String userKey);

    @Nullable
    ApplicationUser getUser(String userKey);

    @Nullable
    ProjectDto getProjectDto(Long id);

    @Nullable
    IssueDto getIssueDto(Long id);
}
