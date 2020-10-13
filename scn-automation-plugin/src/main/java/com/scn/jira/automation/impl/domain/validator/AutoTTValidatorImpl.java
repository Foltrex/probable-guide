package com.scn.jira.automation.impl.domain.validator;

import com.scn.jira.automation.api.domain.service.AutoTTService;
import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.api.domain.service.PermissionService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.api.domain.validator.AutoTTValidator;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.PermissionKey;
import com.scn.jira.automation.impl.domain.dto.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class AutoTTValidatorImpl implements AutoTTValidator {
    private final PermissionService permissionService;
    private final JiraContextService contextService;
    private final WorklogContextService worklogContextService;
    private final AutoTTService autoTTService;

    @Autowired
    public AutoTTValidatorImpl(PermissionService permissionService, JiraContextService contextService,
                               WorklogContextService worklogContextService, AutoTTService autoTTService) {
        this.permissionService = permissionService;
        this.contextService = contextService;
        this.worklogContextService = worklogContextService;
        this.autoTTService = autoTTService;
    }

    @Override
    public Validator validate(@Nonnull AutoTTDto autoTTDto) {
        Validator validator = new Validator();
        List<String> errorMessages = validator.getErrorMessages();

        if (autoTTDto.getUser() == null) {
            errorMessages.add("User has to be set");
            validator.setValid(false);
        } else if (contextService.getUserDto(autoTTDto.getUser().getKey()) == null) {
            errorMessages.add("Couldn't find user");
            validator.setValid(false);
        } else {
            AutoTTDto foundUser = autoTTService.getByUserKey(autoTTDto.getUser().getKey());
            if (foundUser != null && !foundUser.getId().equals(autoTTDto.getId())) {
                errorMessages.add("User already exists in list");
                validator.setValid(false);
            }
        }

        if (autoTTDto.getProject() == null) {
            errorMessages.add("Project has to be set");
            validator.setValid(false);
        } else if (contextService.getProjectDto(autoTTDto.getProject().getId()) == null) {
            errorMessages.add("Couldn't find project");
            validator.setValid(false);
        }

        if (autoTTDto.getIssue() == null) {
            errorMessages.add("Issue has to be set");
            validator.setValid(false);
        } else if (contextService.getIssueDto(autoTTDto.getIssue().getId()) == null) {
            errorMessages.add("Couldn't find issue");
            validator.setValid(false);
        }

        if (autoTTDto.getWorklogType() != null
            && worklogContextService.getWorklogType(autoTTDto.getWorklogType().getId()) == null) {
            errorMessages.add("Couldn't find worklog type");
            validator.setValid(false);
        }

        return validator;
    }

    @Override
    public boolean canView() {
        return permissionService.hasViewPermission(contextService.getCurrentUser());
    }

    @Override
    public boolean canCreate(AutoTTDto autoTTDto) {
        return permissionService.hasPermission(PermissionKey.CREATE, autoTTDto, contextService.getCurrentUser());
    }

    @Override
    public boolean canUpdate(AutoTTDto autoTTDto) {
        return permissionService.hasPermission(PermissionKey.UPDATE, autoTTDto, contextService.getCurrentUser());
    }

    @Override
    public boolean canDelete(Long id) {
        AutoTTDto autoTTDto = autoTTService.get(id);
        if (autoTTDto == null) {
            return false;
        }

        return permissionService.hasPermission(PermissionKey.DELETE, autoTTDto, contextService.getCurrentUser());
    }
}
