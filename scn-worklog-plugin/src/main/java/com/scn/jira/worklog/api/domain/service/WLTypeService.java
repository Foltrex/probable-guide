package com.scn.jira.worklog.api.domain.service;

import com.atlassian.annotations.PublicApi;
import com.scn.jira.worklog.impl.domain.dto.WLTypeDto;
import com.scn.jira.worklog.impl.domain.dto.WorklogTypeDto;
import com.scn.jira.worklog.impl.domain.entity.WLType;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;

@PublicApi
@Validated
public interface WLTypeService {
    List<WLTypeDto> getAll();

    WLTypeDto get(Long id);

    WLTypeDto create(@Valid WLTypeDto wlTypeDto);

    WLTypeDto update(Long id, @Valid WLTypeDto wlTypeDto);

    void deleteById(Long id);
}
