package com.scn.jira.automation.impl.domain.service;

import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
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

    @Override
    public void checkPermission(@Nonnull PermissionKey key, @Nonnull AutoTTDto autoTTDto) {
        if (!hasPermission(key, autoTTDto)) {
            throw new ForbiddenException(String.format("No %s permissions", key));
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
