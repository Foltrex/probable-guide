package com.scn.jira.automation.api.domain.validator;

import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.Validator;

public interface AutoTTValidator {
    Validator validate(AutoTTDto autoTTDto);

    boolean canView();

    boolean canCreate(AutoTTDto autoTTDto);

    boolean canUpdate(AutoTTDto autoTTDto);

    boolean canDelete(Long id);
}
