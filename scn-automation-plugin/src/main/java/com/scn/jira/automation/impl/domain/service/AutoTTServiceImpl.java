package com.scn.jira.automation.impl.domain.service;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.scn.jira.automation.api.domain.service.AutoTTService;
import com.scn.jira.automation.api.domain.service.PermissionProvider;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.PermissionKey;
import com.scn.jira.automation.impl.domain.entity.AutoTT;
import com.scn.jira.automation.impl.domain.mapper.AutoTTMapper;
import com.scn.jira.automation.impl.domain.mapper.JiraDataMapper;
import com.scn.jira.automation.impl.domain.repository.AutoTTRepository;
import com.scn.jira.common.ao.Transactional;
import com.scn.jira.common.exception.EntityNotFoundException;
import com.scn.jira.common.exception.InternalRuntimeException;
import com.scn.jira.common.exception.ObjectValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutoTTServiceImpl implements AutoTTService {
    private final AutoTTRepository autoTTRepository;
    private final JiraAuthenticationContext authenticationContext;
    private final AutoTTMapper autoTTMapper;
    private final JiraDataMapper jiraDataMapper;
    private final PermissionProvider permissionProvider;
    private final ObjectValidator objectValidator;

    @Override
    public List<AutoTTDto> getAll() {
        return autoTTRepository.findAll().stream()
            .map(autoTTMapper::map)
            .filter(dto -> permissionProvider.hasPermission(PermissionKey.READ, dto))
            .sorted(Comparator.comparing(AutoTTDto::getUpdated))
            .collect(Collectors.toList());
    }

    @Override
    public List<AutoTTDto> getAllActive() {
        return autoTTRepository.findAllByActiveTrue().stream().map(autoTTMapper::map).collect(Collectors.toList());
    }

    @Override
    public AutoTTDto get(Long id) {
        AutoTTDto result = autoTTRepository.findById(id).map(autoTTMapper::map).orElseThrow(() -> new EntityNotFoundException(AutoTT.class, id));
        permissionProvider.checkPermission(PermissionKey.READ, result);
        return result;
    }

    @Override
    @Transactional
    public AutoTTDto add(@Nonnull AutoTTDto autoTTDto) {
        objectValidator.validate(autoTTDto);
        if (autoTTDto.getIssue().getId() == null) {
            autoTTDto.setIssue(jiraDataMapper.mapIssueByKey(autoTTDto.getIssue().getKey()));
        }
        permissionProvider.checkPermission(PermissionKey.CREATE, autoTTDto);
        autoTTRepository.findByUserKey(autoTTDto.getUser().getKey()).ifPresent(autoTT -> {
            throw new InternalRuntimeException("Current user auto time tracking configuration already exists");
        });
        AutoTT autoTT = autoTTRepository.create(autoTTMapper.map(autoTTDto));
        return autoTTMapper.map(autoTTRepository.save(autoTTMapper.map(autoTTDto, autoTT, authenticationContext.getLoggedInUser().getKey())));
    }

    @Override
    @Transactional
    public AutoTTDto update(@Nonnull AutoTTDto autoTTDto) {
        objectValidator.validate(autoTTDto);
        if (autoTTDto.getIssue().getId() == null) {
            autoTTDto.setIssue(jiraDataMapper.mapIssueByKey(autoTTDto.getIssue().getKey()));
        }
        permissionProvider.checkPermission(PermissionKey.UPDATE, autoTTDto);
        return autoTTRepository.findById(autoTTDto.getId())
            .map(autoTT -> autoTTMapper.map(autoTTDto, autoTT, authenticationContext.getLoggedInUser().getKey()))
            .map(autoTTRepository::save)
            .map(autoTTMapper::map)
            .orElseThrow(() -> new EntityNotFoundException(AutoTT.class, autoTTDto.getId()));
    }

    @Override
    public void remove(Long id) {
        AutoTT autoTT = autoTTRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(AutoTT.class, id));
        permissionProvider.checkPermission(PermissionKey.DELETE, autoTTMapper.map(autoTT));
        autoTTRepository.delete(autoTT);
    }
}
