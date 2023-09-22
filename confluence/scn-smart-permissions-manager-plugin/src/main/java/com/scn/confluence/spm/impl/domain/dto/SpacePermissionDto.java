package com.scn.confluence.spm.impl.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonAutoDetect;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonAutoDetect
public class SpacePermissionDto {
    private String id;
    private Long spaceId;
    private String spaceKey;
    private String username;
    private Long userId;
    private String permissionLevel;

}
