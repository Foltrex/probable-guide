package com.scn.jira.automation.impl.domain.service;

import com.atlassian.jira.user.ApplicationUser;
import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.api.domain.service.PermissionService;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.PermissionKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final JiraContextService jiraContextService;

    @Override
    public boolean hasPermission(@Nonnull PermissionKey key, @Nonnull AutoTTDto autoTTDto, @Nonnull ApplicationUser user) {
        switch (key) {
            case CREATE:
            case READ:
            case UPDATE:
                return jiraContextService.isCurrentUserAdmin()
                    || autoTTDto.getUser().getKey().equals(user.getKey());
            case DELETE:
                return jiraContextService.isCurrentUserSystemAdmin()
                    || (jiraContextService.isCurrentUserAdmin()
                    && autoTTDto.getAuthor().getKey().equals(user.getKey()));
            default:
                return false;
        }
    }
}
