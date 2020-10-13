package com.scn.jira.automation.api.domain.service;

import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.PermissionKey;

import javax.annotation.Nonnull;

public interface PermissionService {
    boolean hasPermission(@Nonnull PermissionKey key, @Nonnull AutoTTDto autoTTDto, @Nonnull ApplicationUser user);

    boolean hasViewPermission(ApplicationUser user);
}
