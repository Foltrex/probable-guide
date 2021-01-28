package com.scn.jira.automation.api.domain.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

public interface WorklogBackupService {
    @Transactional
    void makeBackup(Long projectId, @Nullable Date from, @Nullable Date to);

    @Transactional
    void restoreBackup(Long projectId, @Nullable Date from, @Nullable Date to);

    List<WorklogDto> getAllByProject(Long projectId, @Nullable Date from, @Nullable Date to);

    List<WorklogDto> getAllScnByProject(Long projectId, @Nullable Date from, @Nullable Date to);
}
