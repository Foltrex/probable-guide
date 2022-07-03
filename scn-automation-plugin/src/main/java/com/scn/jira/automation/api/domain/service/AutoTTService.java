package com.scn.jira.automation.api.domain.service;

import com.atlassian.annotations.PublicApi;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@PublicApi
@Validated
public interface AutoTTService {
    List<AutoTTDto> getAll();

    @Nullable
    AutoTTDto get(Long id);

    AutoTTDto add(@Valid AutoTTDto autoTTDto);

    AutoTTDto update(@Valid AutoTTDto autoTTDto);

    void updateByUserKey(String userKey);

    void startJob();

    void startJob(Long id);

    void remove(Long id);

    void removeAllByIssueId(Long issueId);

    void removeAllByProjectId(Long projectId);

    void removeAllByUsernames(Collection<String> username);

    void removeAllByInvalidConstraint();
}
