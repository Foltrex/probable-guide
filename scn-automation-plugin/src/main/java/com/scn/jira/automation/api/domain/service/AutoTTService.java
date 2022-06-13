package com.scn.jira.automation.api.domain.service;

import com.atlassian.annotations.PublicApi;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;

import javax.annotation.Nullable;
import java.util.List;

@PublicApi
public interface AutoTTService {
    List<AutoTTDto> getAll();

    @Nullable
    AutoTTDto get(Long id);

    AutoTTDto add(AutoTTDto autoTTDto);

    AutoTTDto update(AutoTTDto autoTTDto);

    void remove(Long id);
}
