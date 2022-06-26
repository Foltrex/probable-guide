package com.scn.jira.automation.api.domain.service;

import com.atlassian.annotations.PublicApi;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import com.scn.jira.automation.impl.domain.dto.WorklogTypeDto;
import com.scn.jira.automation.impl.domain.entity.AutoTT;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.List;

@PublicApi
public interface WorklogContextService {

    List<WorklogTypeDto> getAllWorklogTypes();

    void createWorklog(@Nonnull WorklogDto worklog);

    void deleteWorklogById(Long id);

    void doAutoTimeTracking(@Nonnull AutoTT autoTT, LocalDate to);

    void deleteIncorrectWorklogs(); // TODO. Remove.
}
