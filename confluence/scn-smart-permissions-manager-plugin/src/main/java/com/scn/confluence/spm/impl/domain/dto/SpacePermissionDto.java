package com.scn.confluence.spm.impl.domain.dto;

import lombok.Data;

@Data
public class SpacePermissionDto {
    private Long spaceId;
    private String spaceKey;
    private String username;
    private Long userId;
    private String permissionLevel;
}
