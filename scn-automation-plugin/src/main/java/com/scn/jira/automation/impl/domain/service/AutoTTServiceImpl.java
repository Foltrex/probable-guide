package com.scn.jira.automation.impl.domain.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.scn.jira.automation.api.domain.service.AutoTTService;
import com.scn.jira.automation.api.domain.service.PermissionProvider;
import com.scn.jira.automation.api.domain.service.WorklogContextService;
import com.scn.jira.automation.impl.domain.dto.AutoTTDto;
import com.scn.jira.automation.impl.domain.dto.PermissionKey;
import com.scn.jira.automation.impl.domain.dto.UserDto;
import com.scn.jira.automation.impl.domain.entity.AutoTT;
import com.scn.jira.automation.impl.domain.mapper.AutoTTMapper;
import com.scn.jira.automation.impl.domain.mapper.JiraDataMapper;
import com.scn.jira.automation.impl.domain.repository.AutoTTRepository;
import com.scn.jira.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j
public class AutoTTServiceImpl implements AutoTTService {
    private final AutoTTRepository autoTTRepository;
    private final JiraAuthenticationContext authenticationContext;
    private final WorklogContextService worklogContextService;
    private final AutoTTMapper autoTTMapper;
    private final JiraDataMapper jiraDataMapper;
    private final PermissionProvider permissionProvider;

    @Override
    public List<AutoTTDto> getAll() {
        return autoTTRepository.findAll().stream()
            .map(autoTT -> {
                try {
                    return autoTTMapper.map(autoTT);
                } catch (EntityNotFoundException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .filter(dto -> permissionProvider.hasPermission(PermissionKey.READ, dto))
            .sorted(Comparator.comparing(AutoTTDto::getUpdated, Comparator.reverseOrder()))
            .collect(Collectors.toList());
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
        if (autoTTDto.getIssue().getId() == null) {
            autoTTDto.setIssue(jiraDataMapper.mapIssueByKey(autoTTDto.getIssue().getKey()));
        }
        permissionProvider.checkPermission(PermissionKey.CREATE, autoTTDto);
        AutoTT autoTT = autoTTRepository.findByUserKey(autoTTDto.getUser().getKey()).orElseGet(() -> autoTTRepository.create(autoTTMapper.map(autoTTDto)));
        return autoTTMapper.map(autoTTRepository.save(autoTTMapper.map(autoTTDto, autoTT, authenticationContext.getLoggedInUser().getKey())));
    }

    @Override
    @Transactional
    public AutoTTDto update(@Nonnull AutoTTDto autoTTDto) {
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
    public void updateByUserKey(String userKey) {
        autoTTRepository.findByUserKey(userKey).ifPresent(autoTT -> {
            UserDto userDto = jiraDataMapper.mapUserByKey(userKey);
            autoTT.setUsername(userDto.getUsername());
            autoTT.setActive(autoTT.getActive() && userDto.isActive());
            autoTTRepository.save(autoTT);
        });
    }

    @Override
    public void startJob() {
        LocalDate to = LocalDate.now().minusDays(1);
        autoTTRepository.findAllByActiveTrueAndStartDateBefore(Timestamp.valueOf(to.plusDays(1).atStartOfDay())).forEach(value -> {
            try {
                worklogContextService.doAutoTimeTracking(value, to);
                value.setStartDate(Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
                autoTTRepository.save(value);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        });
    }

    @Override
    public void startJob(Long id) {
        AutoTT autoTT = autoTTRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(AutoTT.class, id));
        LocalDate to = LocalDate.now().minusDays(1);
        worklogContextService.doAutoTimeTracking(autoTT, to);
        autoTT.setStartDate(Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        autoTTRepository.save(autoTT);
    }

    @Override
    public void remove(Long id) {
        AutoTT autoTT = autoTTRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(AutoTT.class, id));
        permissionProvider.checkPermission(PermissionKey.DELETE, autoTTMapper.map(autoTT));
        autoTTRepository.delete(autoTT);
    }

    @Override
    public void removeAllByIssueId(Long issueId) {
        autoTTRepository.deleteAllByIssueId(issueId);
    }

    @Override
    public void removeAllByProjectId(Long projectId) {
        autoTTRepository.deleteAllByProjectId(projectId);
    }

    @Override
    public void removeAllByUsernames(Collection<String> username) {
        autoTTRepository.deleteAllByUsernameIn(username);
    }

    @Override
    public void removeAllByInvalidConstraint() {
        autoTTRepository.deleteAll(autoTTRepository.findAll().stream().map(autoTT -> {
            try {
                autoTTMapper.map(autoTT);
                return null;
            } catch (EntityNotFoundException e) {
                return autoTT;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()));
    }
}
