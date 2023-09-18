package com.scn.confluence.spm.impl.domain.dto;

import lombok.Builder;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Data
@Builder
@JsonAutoDetect
public class SpacePermissionDto {
    private Long spaceId;
    private String spaceKey;
    private String username;
    private Long userId;
    private String permissionLevel;
}
