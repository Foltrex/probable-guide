package com.scn.jira.automation.impl.domain.service;

import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.automation.api.domain.service.PermissionService;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.PermissionKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final GlobalPermissionManager globalPermissionManager;

    @Autowired
    public PermissionServiceImpl(GlobalPermissionManager globalPermissionManager) {
        this.globalPermissionManager = globalPermissionManager;
    }

    @Override
    public boolean hasPermission(@Nonnull PermissionKey key, @Nonnull AutoTTDto autoTTDto, @Nonnull ApplicationUser user) {
        switch (key) {
            case CREATE:
            case READ:
            case UPDATE:
                return globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, user)
                    || globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user);
            case DELETE:
                return globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, user)
                    || (globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user)
                    && autoTTDto.getAuthor().getKey().equals(user.getKey()));
            default:
                return false;
        }
    }

    @Override
    public boolean hasViewPermission(ApplicationUser user) {
        return globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, user)
            || globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, user);
    }
}
