package com.scn.jira.automation.api.domain.service;

import com.atlassian.annotations.PublicApi;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import com.scn.jira.automation.impl.domain.dto.WorklogTypeDto;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.List;
import java.util.Set;

@PublicApi
public interface WorklogContextService {
    WorklogTypeDto getWorklogType(String id);

    List<WorklogTypeDto> getAllWorklogTypes();

    String getFormattedTime(Long time);

    Long getParsedTime(String formattedTime);

    boolean isValidFormattedTime(String formattedTime);

    boolean isBlankFormattedTime(String formattedTime);

    Set<Date> getWorkedDays(String userKey, @Nonnull Date from, @Nonnull Date to);

    void createScnWorklog(AutoTTDto autoTTDto, Date date);

    void createWorklog(@Nonnull WorklogDto worklog);

    void deleteWorklogById(Long id);
}
