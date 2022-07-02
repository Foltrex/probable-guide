package com.scn.jira.automation.api.domain.service;

import com.scn.jira.automation.impl.domain.dto.WorklogDto;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface WorklogSQLService {
    List<WorklogDto> getAllByProject(Long projectId, @Nullable Date from, @Nullable Date to);

    List<WorklogDto> getAllScnByProject(Long projecId, @Nullable Date from, @Nullable Date to);

    @Nullable
    Timestamp getTimestampFrom(Date from);

    @Nullable
    Timestamp getTimestampTo(Date to);
}
