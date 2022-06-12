package com.scn.jira.automation.impl.domain.mapper;

import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraDurationUtils;
import com.scn.jira.automation.impl.domain.dto.IssueDto;
import com.scn.jira.automation.impl.domain.dto.ProjectDto;
import com.scn.jira.automation.impl.domain.dto.UserDto;
import com.scn.jira.automation.impl.domain.dto.WorklogTypeDto;
import com.scn.jira.common.exception.EntityNotFoundException;
import com.scn.jira.common.exception.InternalRuntimeException;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Log4j
public class JiraDataMapper {
    private final ExtendedConstantsManager extendedConstantsManager;
    private final UserManager userManager;
    private final ProjectManager projectManager;
    private final IssueManager issueManager;
    private final JiraDurationUtils jiraDurationUtils;

    public UserDto mapUserByKey(String userKey) {
        ApplicationUser user = userManager.getUserByKey(userKey);
        if (user == null) {
            throw new EntityNotFoundException(ApplicationUser.class, "key", userKey);
        }
        return new UserDto(user.getKey(), user.getDisplayName(), user.getUsername());
    }

    public ProjectDto mapProjectById(Long id) {
        Project project = projectManager.getProjectObj(id);
        if (project == null) {
            throw new EntityNotFoundException(Project.class, id);
        }
        return new ProjectDto(project.getId(), project.getKey(), project.getName());
    }

    public IssueDto mapIssueById(Long id) {
        MutableIssue issue = issueManager.getIssueObject(id);
        if (issue == null) {
            throw new EntityNotFoundException(Issue.class, id);
        }
        return new IssueDto(issue.getId(), issue.getKey(), issue.getSummary());
    }

    public IssueDto mapIssueByKey(String key) {
        MutableIssue issue = issueManager.getIssueObject(key);
        if (issue == null) {
            throw new EntityNotFoundException(Issue.class, "key", key);
        }
        return new IssueDto(issue.getId(), issue.getKey(), issue.getSummary());
    }

    public WorklogTypeDto mapWorklogTypeById(String id) {
        WorklogType worklogType = extendedConstantsManager.getWorklogTypeObject(id);
        if (worklogType == null) {
            throw new EntityNotFoundException(WorklogType.class, id);
        }
        return new WorklogTypeDto(worklogType.getId(), worklogType.getName());
    }

    public String mapTime(Long time) {
        return jiraDurationUtils.getShortFormattedDuration(time, Locale.ENGLISH);
    }

    public Long mapTime(String time) {
        try {
            return jiraDurationUtils.parseDuration(time, Locale.ENGLISH);
        } catch (InvalidDurationException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new InternalRuntimeException(e.getLocalizedMessage(), e);
        }
    }
}
