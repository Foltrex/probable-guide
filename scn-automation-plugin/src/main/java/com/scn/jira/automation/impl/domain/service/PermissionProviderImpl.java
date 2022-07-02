package com.scn.jira.automation.impl.domain.service;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.automation.api.domain.service.PermissionProvider;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.PermissionKey;
import com.scn.jira.common.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
@RequiredArgsConstructor
public class PermissionProviderImpl implements PermissionProvider {
    private final JiraAuthenticationContext authenticationContext;
    private final GlobalPermissionManager globalPermissionManager;
    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final UserManager userManager;

    @Override
    public void checkPermission(@Nonnull PermissionKey key, @Nonnull AutoTTDto autoTTDto) {
        ApplicationUser user = userManager.getUserByKey(autoTTDto.getUser().getKey());
        if (!hasPermission(key, autoTTDto)) {
            throw new ForbiddenException(String.format("No %s permissions", key));
        } else if ((PermissionKey.CREATE.equals(key) || PermissionKey.UPDATE.equals(key))
            && user != null
            && !permissionManager.hasPermission(ProjectPermissions.EDIT_OWN_WORKLOGS,
            issueManager.getIssueObject(autoTTDto.getIssue().getId()), user)) {
            throw new ForbiddenException(String.format("User \"%s\" hasn't permission to add worklog for issue with key=\"%s\"",
                user.getUsername(), autoTTDto.getIssue().getKey()));
        }
    }

    @Override
    public boolean hasPermission(@Nonnull PermissionKey key, @Nonnull AutoTTDto autoTTDto) {
        switch (key) {
            case CREATE:
            case READ:
            case UPDATE:
                return isCurrentUserAdmin()
                    || autoTTDto.getUser().getKey().equals(authenticationContext.getLoggedInUser().getKey());
            case DELETE:
                return isCurrentUserSystemAdmin()
                    || (isCurrentUserAdmin()
                    && autoTTDto.getAuthor().getKey().equals(authenticationContext.getLoggedInUser().getKey()));
            default:
                throw new ForbiddenException("No permissions");
        }
    }

    @Override
    public boolean isCurrentUserAdmin() {
        return globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, authenticationContext.getLoggedInUser())
            || globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, authenticationContext.getLoggedInUser())
            || "akalaputs".equals(authenticationContext.getLoggedInUser().getUsername());
    }

    @Override
    public boolean isCurrentUserSystemAdmin() {
        return globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, authenticationContext.getLoggedInUser());
    }
}
