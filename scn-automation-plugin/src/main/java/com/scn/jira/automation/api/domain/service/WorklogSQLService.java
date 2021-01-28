package com.scn.jira.automation.api.domain.service;

import com.scn.jira.automation.impl.domain.dto.WorklogDto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface WorklogSQLService {
    List<WorklogDto> getAllByProject(Long projectId, @Nullable Date from, @Nullable Date to);

    List<WorklogDto> getAllScnByProject(Long projecId, @Nullable Date from, @Nullable Date to);

    @Nonnull
    Timestamp getTimestampFrom(@Nonnull Date from);

    @Nonnull
    Timestamp getTimestampTo(@Nonnull Date to);
}
