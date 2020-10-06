package com.scn.jira.automation.api.domain.service;

import com.atlassian.annotations.PublicApi;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;

import javax.annotation.Nullable;
import java.util.List;

@PublicApi
public interface AutoTTService {
    List<AutoTTDto> getAll();

    List<AutoTTDto> getAllActive();

    @Nullable
    AutoTTDto get(Long id);

    @Nullable
    AutoTTDto getByUserKey(String key);

    AutoTTDto add(AutoTTDto autoTTDto);

    AutoTTDto update(AutoTTDto autoTTDto);

    void remove(Long id);
}
