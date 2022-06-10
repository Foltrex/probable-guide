package com.scn.jira.automation.api.domain.service;

import com.scn.jira.automation.impl.domain.dto.WorklogDto;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

public interface WorklogBackupService {
    void makeBackup(Long projectId, @Nullable Date from, @Nullable Date to);

    void restoreBackup(Long projectId, @Nullable Date from, @Nullable Date to);

    List<WorklogDto> getAllByProject(Long projectId, @Nullable Date from, @Nullable Date to);

    List<WorklogDto> getAllScnByProject(Long projectId, @Nullable Date from, @Nullable Date to);
}
