package com.scn.jira.automation.impl.domain.service;

import com.scn.jira.automation.api.domain.service.AutoTTService;
import com.scn.jira.automation.api.domain.service.JiraContextService;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.entity.AutoTT;
import com.scn.jira.automation.impl.domain.repository.AutoTTRepository;
import com.scn.jira.common.ao.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutoTTServiceImpl implements AutoTTService {
    private final AutoTTRepository autoTTRepository;
    private final JiraContextService contextService;
    private final WorklogContextService worklogContextService;

    @Override
    public List<AutoTTDto> getAll() {
        return autoTTRepository.findAll().stream().map(autoTT -> new AutoTTDto(contextService, worklogContextService, autoTT)).collect(Collectors.toList());
    }

    @Override
    public List<AutoTTDto> getAllActive() {
        return autoTTRepository.findAllByActiveTrue().stream().map(autoTT -> new AutoTTDto(contextService, worklogContextService, autoTT)).collect(Collectors.toList());
    }

    @Override
    public AutoTTDto get(Long id) {
        return autoTTRepository.findById(id).map(autoTT -> new AutoTTDto(contextService, worklogContextService, autoTT)).orElse(null);
    }

    @Override
    public AutoTTDto getByUserKey(String key) {
        return autoTTRepository.findByByUserKey(key).map(autoTT -> new AutoTTDto(contextService, worklogContextService, autoTT)).orElse(null);
    }

    @Override
    @Transactional
    public AutoTTDto add(@Nonnull AutoTTDto autoTTDto) {
        AutoTT autoTT = autoTTRepository.create(autoTTDto.getUser().getKey(), autoTTDto.getProject().getId(), autoTTDto.getIssue().getId(), worklogContextService.getParsedTime(autoTTDto.getRatedTime()));
        this.copyDtoFields(autoTT, autoTTDto);
        autoTT.setCreated(new Timestamp(new Date().getTime()));
        autoTT.setAuthorKey(contextService.getUserDto().getKey());
        return new AutoTTDto(contextService, worklogContextService, autoTTRepository.save(autoTT));
    }

    @Override
    @Transactional
    public AutoTTDto update(@Nonnull AutoTTDto autoTTDto) {
        return autoTTRepository.findById(autoTTDto.getId()).map(autoTT -> {
            this.copyDtoFields(autoTT, autoTTDto);
            return new AutoTTDto(contextService, worklogContextService, autoTTRepository.save(autoTT));
        }).orElse(null);
    }

    @Override
    public void remove(Long id) {
        autoTTRepository.deleteById(id);
    }

    private void copyDtoFields(@Nonnull AutoTT autoTT, @Nonnull AutoTTDto autoTTDto) {
        autoTT.setUserKey(autoTTDto.getUser().getKey());
        autoTT.setProjectId(autoTTDto.getProject().getId());
        autoTT.setIssueId(autoTTDto.getIssue().getId());
        autoTT.setWorklogTypeId(autoTTDto.getWorklogType() == null ? null : autoTTDto.getWorklogType().getId());
        autoTT.setRatedTime(worklogContextService.getParsedTime(autoTTDto.getRatedTime()));
        autoTT.setActive(autoTTDto.isActive());
        autoTT.setUpdated(new Timestamp(new Date().getTime()));
        autoTT.setUpdateAuthorKey(contextService.getUserDto().getKey());
    }
}
