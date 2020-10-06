package com.scn.jira.automation.impl.domain.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.scn.jira.automation.api.domain.service.AutoTTService;
import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.entity.AutoTT;
import net.java.ao.DBParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ExportAsService(AutoTTService.class)
public class AutoTTServiceImpl implements AutoTTService {
    private final ActiveObjects ao;
    private final JiraContextService contextService;
    private final WorklogContextService worklogContextService;

    @Autowired
    public AutoTTServiceImpl(ActiveObjects ao, JiraContextService contextService,
                             WorklogContextService worklogContextService) {
        this.ao = ao;
        this.contextService = contextService;
        this.worklogContextService = worklogContextService;
    }

    @Override
    public List<AutoTTDto> getAll() {
        AutoTT[] autoTTs = this.ao.find(AutoTT.class);
        return Stream.of(autoTTs).map(autoTT -> new AutoTTDto(contextService, worklogContextService, autoTT)).collect(Collectors.toList());
    }

    @Override
    public List<AutoTTDto> getAllActive() {
        AutoTT[] autoTTs = this.ao.find(AutoTT.class, "ACTIVE = ?", true);
        return Stream.of(autoTTs).map(autoTT -> new AutoTTDto(contextService, worklogContextService, autoTT)).collect(Collectors.toList());
    }

    @Override
    public AutoTTDto get(Long id) {
        AutoTT autoTT = ao.get(AutoTT.class, id);
        return new AutoTTDto(contextService, worklogContextService, autoTT);
    }

    @Override
    public AutoTTDto getByUserKey(String key) {
        AutoTT[] autoTTusers = this.ao.find(AutoTT.class, "USER_KEY = ?", key);
        return autoTTusers != null && autoTTusers.length > 0 ? new AutoTTDto(contextService, worklogContextService, autoTTusers[0]) : null;
    }

    @Override
    @Transactional
    public AutoTTDto add(AutoTTDto autoTTDto) {
        AutoTT autoTT = ao.create(AutoTT.class,
            new DBParam("USER_KEY", autoTTDto.getUser().getKey()),
            new DBParam("PROJECT_ID", autoTTDto.getProject().getId()),
            new DBParam("ISSUE_ID", autoTTDto.getIssue().getId())
        );
        this.copyDtoFields(autoTT, autoTTDto);
        autoTT.setCreated(new Timestamp(new Date().getTime()));
        autoTT.setAuthorKey(contextService.getUserDto().getKey());
        autoTT.save();
        return new AutoTTDto(contextService, worklogContextService, autoTT);
    }

    @Override
    @Transactional
    public AutoTTDto update(@Nonnull AutoTTDto autoTTDto) {
        AutoTT autoTT = ao.get(AutoTT.class, autoTTDto.getId());
        this.copyDtoFields(autoTT, autoTTDto);
        autoTT.save();
        return new AutoTTDto(contextService, worklogContextService, autoTT);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        ao.delete(ao.get(AutoTT.class, id));
    }

    private void copyDtoFields(@Nonnull AutoTT autoTT, @Nonnull AutoTTDto autoTTDto) {
        autoTT.setUserKey(autoTTDto.getUser().getKey());
        autoTT.setProjectId(autoTTDto.getProject().getId());
        autoTT.setIssueId(autoTTDto.getIssue().getId());
        autoTT.setWorklogTypeId(autoTTDto.getWorklogType() == null ? null : autoTTDto.getWorklogType().getId());
        autoTT.setActive(autoTTDto.isActive());
        autoTT.setUpdated(new Timestamp(new Date().getTime()));
        autoTT.setUpdateAuthorKey(contextService.getUserDto().getKey());
    }
}
