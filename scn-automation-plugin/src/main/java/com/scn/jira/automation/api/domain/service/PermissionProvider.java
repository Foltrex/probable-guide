package com.scn.jira.automation.api.domain.service;

import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.PermissionKey;

import javax.annotation.Nonnull;

public interface PermissionProvider {

    void checkPermission(@Nonnull PermissionKey key, @Nonnull AutoTTDto autoTTDto);

    boolean hasPermission(@Nonnull PermissionKey key, @Nonnull AutoTTDto autoTTDto);

    boolean isCurrentUserAdmin();

    boolean isCurrentUserSystemAdmin();
}
