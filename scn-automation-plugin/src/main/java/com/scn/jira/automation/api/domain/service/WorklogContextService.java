package com.scn.jira.automation.api.domain.service;

import com.atlassian.annotations.PublicApi;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import com.scn.jira.automation.impl.domain.dto.WorklogTypeDto;
import com.scn.jira.automation.impl.domain.entity.AutoTT;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

@PublicApi
public interface WorklogContextService {

    List<WorklogTypeDto> getAllWorklogTypes();

    Set<Date> getWorkedDays(String userKey, @Nonnull LocalDate from, @Nonnull LocalDate to);

    void createScnWorklog(AutoTT autoTT, Date date);

    void createWorklog(@Nonnull WorklogDto worklog);

    void deleteWorklogById(Long id);
}
