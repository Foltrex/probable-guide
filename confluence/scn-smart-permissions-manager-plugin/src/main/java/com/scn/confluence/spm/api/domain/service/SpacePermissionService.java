package com.scn.confluence.spm.api.domain.service;

import com.scn.confluence.spm.impl.domain.dto.SpacePermissionDto;

import java.util.List;

public interface SpacePermissionService {
    List<SpacePermissionDto> getSpacePermissionBySpaceKey(String spaceKey);

    List<SpacePermissionDto> getSpacePermissions();
}
