package com.scn.jira.automation.api.domain.service;

import com.scn.jira.automation.impl.domain.dto.WorklogDto;

import java.util.List;

public interface WorklogService {
    List<WorklogDto> getAllByProject(Long projectId);

    List<WorklogDto> getAllScnByProject(Long projecId);
}
