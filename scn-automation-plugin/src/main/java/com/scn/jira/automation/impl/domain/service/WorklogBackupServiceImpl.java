package com.scn.jira.automation.impl.domain.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.scn.jira.automation.api.domain.service.WorklogBackupService;
import com.scn.jira.automation.api.domain.service.WorklogSQLService;
import com.scn.jira.automation.impl.domain.dto.WorklogDto;
import com.scn.jira.automation.impl.domain.entity.ScnWorklog;
import com.scn.jira.automation.impl.domain.entity.Worklog;
import lombok.RequiredArgsConstructor;
import net.java.ao.DBParam;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class WorklogBackupServiceImpl implements WorklogBackupService {
    private final ActiveObjects ao;
    private final WorklogSQLService worklogSQLService;

    @Override
    public void makeBackup(Long projectId, @Nullable Date from, @Nullable Date to) {
        this.clearWorklogBackup(projectId, from, to);
        this.makeWorklogBackup(projectId, from, to);
        this.clearScnWorklogBackup(projectId, from, to);
        this.makeScnWorklogBackup(projectId, from, to);
    }

    @Override
    public void restoreBackup(Long projectId, @Nullable Date from, @Nullable Date to) {

    }

    @Override
    public List<WorklogDto> getAllByProject(Long projectId, @Nullable Date from, @Nullable Date to) {
        Worklog[] worklogs = ao.find(Worklog.class, "PROJECT_ID = ?"
                + (from == null ? " AND ? IS NULL" : " AND START_DATE >= ?")
                + (to == null ? " AND ? IS NULL" : " AND START_DATE <= ?"),
            projectId,
            from == null ? null : worklogSQLService.getTimestampFrom(from),
            to == null ? null : worklogSQLService.getTimestampTo(to)
        );

        return Stream.of(worklogs).map(WorklogDto::new).collect(Collectors.toList());
    }

    @Override
    public List<WorklogDto> getAllScnByProject(Long projectId, @Nullable Date from, @Nullable Date to) {
        ScnWorklog[] scnWorklogs = ao.find(ScnWorklog.class, "PROJECT_ID = ?"
                + (from == null ? " AND ? IS NULL" : " AND START_DATE >= ?")
                + (to == null ? " AND ? IS NULL" : " AND START_DATE <= ?"),
            projectId,
            from == null ? null : worklogSQLService.getTimestampFrom(from),
            to == null ? null : worklogSQLService.getTimestampTo(to)
        );

        return Stream.of(scnWorklogs).map(WorklogDto::new).collect(Collectors.toList());
    }

    private void clearWorklogBackup(Long projectId, @Nullable Date from, @Nullable Date to) {
        Worklog[] worklogs = ao.find(Worklog.class, "PROJECT_ID = ?"
                + (from == null ? " AND ? IS NULL" : " AND START_DATE >= ?")
                + (to == null ? " AND ? IS NULL" : " AND START_DATE <= ?"),
            projectId,
            from == null ? null : worklogSQLService.getTimestampFrom(from),
            to == null ? null : worklogSQLService.getTimestampTo(to)
        );
        ao.delete(worklogs);
    }

    private void clearScnWorklogBackup(Long projectId, @Nullable Date from, @Nullable Date to) {
        ScnWorklog[] scnWorklogs = ao.find(ScnWorklog.class, "PROJECT_ID = ?"
                + (from == null ? " AND ? IS NULL" : " AND START_DATE >= ?")
                + (to == null ? " AND ? IS NULL" : " AND START_DATE <= ?"),
            projectId,
            from == null ? null : worklogSQLService.getTimestampFrom(from),
            to == null ? null : worklogSQLService.getTimestampTo(to)
        );
        ao.delete(scnWorklogs);
    }

    private void makeWorklogBackup(Long projectId, @Nullable Date from, @Nullable Date to) {
        List<WorklogDto> worklogs = worklogSQLService.getAllByProject(projectId, from, to);
        worklogs.forEach(worklogDto -> {
            Worklog worklog = ao.create(Worklog.class,
                new DBParam("ID", worklogDto.getId()),
                new DBParam("ISSUE_ID", worklogDto.getIssueId()),
                new DBParam("PROJECT_ID", worklogDto.getProjectId()));
            this.copyAOFields(worklog, worklogDto);
            worklog.save();
        });
    }

    private void makeScnWorklogBackup(Long projectId, @Nullable Date from, @Nullable Date to) {
        List<WorklogDto> worklogs = worklogSQLService.getAllScnByProject(projectId, from, to);
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
