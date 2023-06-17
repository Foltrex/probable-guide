package com.scn.jira.worklog.impl.domain.mapper;

import com.scn.jira.worklog.impl.domain.dto.WLTypeDto;
import com.scn.jira.worklog.impl.domain.entity.WLType;
import net.java.ao.DBParam;

import javax.annotation.Nonnull;

public class WLTypeMapper {

    public WLType mapToWLType(WLTypeDto wlTypeDto, @Nonnull WLType wlType) {
        wlType.setName(wlTypeDto.getName());
        wlType.setDescription(wlTypeDto.getDescription());
        wlType.setIconUri(wlTypeDto.getIconUri());
        wlType.setStatusColor(wlType.getStatusColor());
        return wlType;
    }

    public WLTypeDto mapToWLTypeDto(WLType wlType) {
        return WLTypeDto.builder()
            .id(wlType.getId())
            .name(wlType.getName())
            .description(wlType.getDescription())
            .iconUri(wlType.getIconUri())
            .statusColor(wlType.getStatusColor())
            .build();
    }

    public DBParam[] mapToDBParam(WLTypeDto wlTypeDto) {
        if (wlTypeDto == null) {
            return new DBParam[]{};
        }

        return new DBParam[] {
            new DBParam("ID", wlTypeDto.getId()),
            new DBParam("NAME", wlTypeDto.getName()),
            new DBParam("DESCRIPTION", wlTypeDto.getDescription()),
            new DBParam("ICON_URI", wlTypeDto.getIconUri()),
            new DBParam("STATUS_COLOR", wlTypeDto.getStatusColor())
        };
    }
}
