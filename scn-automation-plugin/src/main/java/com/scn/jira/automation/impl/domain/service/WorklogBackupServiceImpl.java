package com.scn.jira.automation.impl.domain.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.scn.jira.automation.api.domain.service.WorklogBackupService;
import com.scn.jira.automation.api.domain.service.WorklogSQLService;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import com.scn.jira.automation.impl.domain.entity.ScnWorklog;
import com.scn.jira.automation.impl.domain.entity.Worklog;
import com.scn.jira.automation.impl.domain.repository.ScnWorklogBackupRepository;
import com.scn.jira.automation.impl.domain.repository.WorklogBackupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorklogBackupServiceImpl implements WorklogBackupService {
    private final WorklogBackupRepository worklogRepository;
    private final ScnWorklogBackupRepository scnWorklogRepository;
    private final WorklogSQLService worklogSQLService;

    @Override
    @Transactional
    public void makeBackup(Long projectId, @Nullable Date from, @Nullable Date to) {
        worklogRepository.deleteAllByProjectIdAndStartDateBetween(projectId, worklogSQLService.getTimestampFrom(from), worklogSQLService.getTimestampTo(to));
        this.makeWorklogBackup(projectId, from, to);
        scnWorklogRepository.deleteAllByProjectIdAndStartDateBetween(projectId, worklogSQLService.getTimestampFrom(from), worklogSQLService.getTimestampTo(to));
        this.makeScnWorklogBackup(projectId, from, to);
    }

    @Override
    public void restoreBackup(Long projectId, @Nullable Date from, @Nullable Date to) {

    }

    @Override
    public List<WorklogDto> getAllByProject(Long projectId, @Nullable Date from, @Nullable Date to) {
        return worklogRepository.findAllByProjectIdAndStartDateBetween(projectId, worklogSQLService.getTimestampFrom(from), worklogSQLService.getTimestampTo(to)).stream()
            .map(WorklogDto::new).collect(Collectors.toList());
    }

    @Override
    public List<WorklogDto> getAllScnByProject(Long projectId, @Nullable Date from, @Nullable Date to) {
        return scnWorklogRepository.findAllByProjectIdAndStartDateBetween(projectId, worklogSQLService.getTimestampFrom(from), worklogSQLService.getTimestampTo(to)).stream()
            .map(WorklogDto::new).collect(Collectors.toList());
    }

    private void makeWorklogBackup(Long projectId, @Nullable Date from, @Nullable Date to) {
        List<WorklogDto> worklogs = worklogSQLService.getAllByProject(projectId, from, to);
        worklogs.forEach(worklogDto -> {
            Worklog worklog = worklogRepository.create(worklogDto.getId(), worklogDto.getIssueId(), worklogDto.getProjectId());
            this.copyAOFields(worklog, worklogDto);
            worklogRepository.save(worklog);
        });
    }

    private void makeScnWorklogBackup(Long projectId, @Nullable Date from, @Nullable Date to) {
        List<WorklogDto> worklogs = worklogSQLService.getAllScnByProject(projectId, from, to);
        worklogs.forEach(worklogDto -> {
            ScnWorklog scnWorklog = scnWorklogRepository.create(worklogDto.getId(), worklogDto.getIssueId(), worklogDto.getProjectId());
            this.copyAOFields(scnWorklog, worklogDto);
            scnWorklog.setWorklog(worklogRepository.findById(worklogDto.getLinkedWorklogId()).orElse(null));
            scnWorklogRepository.save(scnWorklog);
        });
    }

    private void copyAOFields(@Nonnull Worklog worklog, @Nonnull WorklogDto worklogDto) {
        worklog.setWorklogBody(worklogDto.getWorklogBody());
        worklog.setStartDate(worklogDto.getStartDate());
        worklog.setTimeWorked(worklogDto.getTimeWorked());
        worklog.setWorklogTypeId(worklogDto.getWorklogTypeId());
        worklog.setAuthorKey(worklogDto.getAuthorKey());
        worklog.setUpdateAuthorKey(worklogDto.getUpdateAuthorKey());
        worklog.setCreated(worklogDto.getCreated());
        worklog.setUpdated(worklogDto.getUpdated());
    }
}
