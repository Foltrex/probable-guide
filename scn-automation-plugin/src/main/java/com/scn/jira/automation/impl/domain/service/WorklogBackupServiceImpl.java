package com.scn.jira.automation.impl.domain.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.scn.jira.automation.api.domain.service.WorklogBackupService;
import com.scn.jira.automation.api.domain.service.WorklogService;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import com.scn.jira.automation.impl.domain.entity.ScnWorklog;
import com.scn.jira.automation.impl.domain.entity.Worklog;
import net.java.ao.DBParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WorklogBackupServiceImpl implements WorklogBackupService {
    private final ActiveObjects ao;
    private final WorklogService worklogService;

    @Autowired
    public WorklogBackupServiceImpl(ActiveObjects ao, WorklogService worklogService) {
        this.ao = ao;
        this.worklogService = worklogService;
    }

    @Override
    public void makeBackup(Long projectId) {
        this.clearWorklogBackup(projectId);
        this.makeWorklogBackup(projectId);
        this.clearScnWorklogBackup(projectId);
        this.makeScnWorklogBackup(projectId);
    }

    @Override
    public void restoreBackup(Long projectId) {
        //TODO. restore from backup.
    }

    @Override
    public List<WorklogDto> getAllByProject(Long projectId, @Nonnull Date from, @Nonnull Date to) {
        Worklog[] worklogs = ao.find(Worklog.class, "PROJECT_ID = ? AND START_DATE BETWEEN ? AND ?",
            projectId,
            new Timestamp(from.getTime()),
            Timestamp.valueOf(to.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusDays(1).minusNanos(1))
        );

        return Stream.of(worklogs).map(WorklogDto::new).collect(Collectors.toList());
    }

    @Override
    public List<WorklogDto> getAllScnByProject(Long projectId, @Nonnull Date from, @Nonnull Date to) {
        ScnWorklog[] worklogs = ao.find(ScnWorklog.class, "PROJECT_ID = ? AND START_DATE BETWEEN ? AND ?",
            projectId,
            new Timestamp(from.getTime()),
            Timestamp.valueOf(to.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusDays(1).minusNanos(1))
        );

        return Stream.of(worklogs).map(WorklogDto::new).collect(Collectors.toList());
    }

    private void clearWorklogBackup(Long projectId) {
        Worklog[] worklogs = ao.find(Worklog.class, "PROJECT_ID = ?", projectId);
        ao.delete(worklogs);
    }

    private void clearScnWorklogBackup(Long projectId) {
        ScnWorklog[] scnWorklogs = ao.find(ScnWorklog.class, "PROJECT_ID = ?", projectId);
        ao.delete(scnWorklogs);
    }

    private void makeWorklogBackup(Long projectId) {
        List<WorklogDto> worklogs = worklogService.getAllByProject(projectId);
        worklogs.forEach(worklogDto -> {
            Worklog worklog = ao.create(Worklog.class,
                new DBParam("ID", worklogDto.getId()),
                new DBParam("ISSUE_ID", worklogDto.getIssueId()),
                new DBParam("PROJECT_ID", worklogDto.getProjectId()));
            this.copyAOFields(worklog, worklogDto);
            worklog.save();
        });
    }

    private void makeScnWorklogBackup(Long projectId) {
        List<WorklogDto> worklogs = worklogService.getAllScnByProject(projectId);
        worklogs.forEach(worklogDto -> {
            ScnWorklog scnWorklog = ao.create(ScnWorklog.class,
                new DBParam("ID", worklogDto.getId()),
                new DBParam("ISSUE_ID", worklogDto.getIssueId()),
                new DBParam("PROJECT_ID", worklogDto.getProjectId()));
            this.copyAOFields(scnWorklog, worklogDto);
            scnWorklog.setWorklog(ao.get(Worklog.class, worklogDto.getLinkedWorklogId()));
            scnWorklog.save();
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
