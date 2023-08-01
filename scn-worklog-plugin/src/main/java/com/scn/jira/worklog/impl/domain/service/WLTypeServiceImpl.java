package com.scn.jira.worklog.impl.domain.service;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.scn.jira.common.exception.EntityNotFoundException;
import com.scn.jira.worklog.api.domain.service.WLTypeService;
import com.scn.jira.worklog.impl.domain.dto.WLTypeDto;
import com.scn.jira.worklog.impl.domain.entity.WLType;
import com.scn.jira.worklog.impl.domain.mapper.WLTypeMapper;
import com.scn.jira.worklog.impl.domain.repository.WLTypeRepository;
import lombok.RequiredArgsConstructor;
import net.java.ao.DBParam;
import net.java.ao.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WLTypeServiceImpl implements WLTypeService {
    private final WLTypeRepository wlTypeRepository;
    private final WLTypeMapper wlTypeMapper = new WLTypeMapper();

    @Override
    public List<WLTypeDto> getAll(String column, String order) {
        return wlTypeRepository.findAllSortedByColumnInOrder(column, order)
            .stream()
            .map(wlTypeMapper::mapToWLTypeDto)
            .collect(Collectors.toList());
    }

    @Override
    public WLTypeDto get(Long id) {
        return wlTypeRepository.findById(id)
            .map(wlTypeMapper::mapToWLTypeDto)
            .orElseThrow(() -> new EntityNotFoundException(WLType.class,id));
    }

    @Override
    public WLTypeDto create(WLTypeDto wlTypeDto) {
        Random random = new Random();

        DBParam[] wlTypeDBParams = wlTypeMapper.mapToDBParam(wlTypeDto);
        WLType createdWlType = wlTypeRepository.create(wlTypeDBParams);
        createdWlType.setSequence(random.nextInt());
        WLType persistedWLType = wlTypeRepository.save(createdWlType);
        return wlTypeMapper.mapToWLTypeDto(persistedWLType);
    }

    @Override
    public WLTypeDto update(Long id, WLTypeDto wlTypeDto) {
        WLType wlType = wlTypeRepository.findById(id)
            .orElseThrow(NoSuchElementException::new);
        WLType mappedWlType = wlTypeMapper.mapToWLType(wlTypeDto, wlType);
        WLType persistedWLType = wlTypeRepository.save(mappedWlType);
        return wlTypeMapper.mapToWLTypeDto(persistedWLType);
    }

    @Override
    public void deleteById(Long id) {
        wlTypeRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        wlTypeRepository.deleteAll();
    }
}
