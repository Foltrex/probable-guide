package com.scn.jira.automation.impl.domain.mapper;

import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.entity.AutoTT;
import lombok.RequiredArgsConstructor;
import net.java.ao.DBParam;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class AutoTTMapper {
    private final JiraDataMapper jiraDataMapper;

    public AutoTTDto map(AutoTT entity) {
        if (entity == null) {
            return null;
        }

        AutoTTDto result = new AutoTTDto();
        result.setId(entity.getId());
        result.setUser(jiraDataMapper.mapUserByKey(entity.getUserKey()));
        result.setProject(jiraDataMapper.mapProjectById(entity.getProjectId()));
        result.setIssue(jiraDataMapper.mapIssueById(entity.getIssueId()));
        result.setWorklogType(jiraDataMapper.mapWorklogTypeById(entity.getWorklogTypeId()));
        result.setRatedTime(jiraDataMapper.mapTime(entity.getRatedTime()));
        result.setStartDate(entity.getStartDate().toLocalDateTime().toLocalDate());
        result.setActive(entity.getActive());
        result.setAuthor(jiraDataMapper.mapUserByKey(entity.getAuthorKey()));
        result.setUpdateAuthor(jiraDataMapper.mapUserByKey(entity.getUpdateAuthorKey()));
        result.setCreated(entity.getCreated());
        result.setUpdated(entity.getUpdated());

        return result;
    }

    public AutoTT map(AutoTTDto dto, @Nonnull AutoTT entity, String currentUserKey) {
        if (dto == null) {
            return null;
        }
        Timestamp currentDate = new Timestamp(new Date().getTime());
        entity.setUserKey(dto.getUser().getKey());
        entity.setUsername(jiraDataMapper.mapUserByKey(dto.getUser().getKey()).getUsername());
        entity.setProjectId(dto.getProject().getId());
        entity.setIssueId(dto.getIssue().getId());
        entity.setWorklogTypeId(dto.getWorklogType() == null ? null : dto.getWorklogType().getId());
        entity.setRatedTime(jiraDataMapper.mapTime(dto.getRatedTime()));
        entity.setStartDate(Timestamp.valueOf(dto.getStartDate().atStartOfDay()));
        entity.setActive(dto.isActive());
        entity.setUpdated(currentDate);
        entity.setUpdateAuthorKey(currentUserKey);
        if (entity.getCreated() == null || entity.getAuthorKey() == null) {
            entity.setCreated(currentDate);
            entity.setAuthorKey(currentUserKey);
        }

        return entity;
    }

    public DBParam[] map(AutoTTDto dto) {
        if (dto == null) {
            return new DBParam[]{};
        }
        return new DBParam[]{new DBParam("USER_KEY", dto.getUser().getKey()),
            new DBParam("USERNAME", jiraDataMapper.mapUserByKey(dto.getUser().getKey()).getUsername()),
            new DBParam("PROJECT_ID", dto.getProject().getId()),
            new DBParam("ISSUE_ID", dto.getIssue().getId()),
            new DBParam("WORKLOG_TYPE_ID", dto.getWorklogType().getId()),
            new DBParam("RATED_TIME", jiraDataMapper.mapTime(dto.getRatedTime())),
            new DBParam("START_DATE", Timestamp.valueOf(dto.getStartDate().atStartOfDay()))};
    }
}
