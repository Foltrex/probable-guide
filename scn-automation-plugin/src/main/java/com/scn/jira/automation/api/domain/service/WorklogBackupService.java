package com.scn.jira.automation.api.domain.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.List;

public interface WorklogBackupService {
    @Transactional
    void makeBackup(Long projectId);

    @Transactional
    void restoreBackup(Long projectId);

    List<WorklogDto> getAllByProject(Long projectId, @Nonnull Date from, @Nonnull Date to);

    List<WorklogDto> getAllScnByProject(Long projectId, @Nonnull Date from, @Nonnull Date to);
}
