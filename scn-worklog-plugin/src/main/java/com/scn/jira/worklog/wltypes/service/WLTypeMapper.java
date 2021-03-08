package com.scn.jira.worklog.wltypes.service;

import com.scn.jira.worklog.wltypes.dal.WLTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper
public interface WLTypeMapper {
    @Mappings({
        @Mapping(source = "ID", target = "id"),
        @Mapping(source = "name", target = "name"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "sequence", target = "sequence")
    })
    WLTypeModel WLTypeEntityToWLTypeModel(WLTypeEntity entity);
//    @InheritInverseConfiguration
//    @Mapping(target = "ID", ignore=true)
//    WLTypeEntity WLTypeModelToWLTypeEntity(WLTypeModel model);

    List<WLTypeModel> WLTypeEntityToWLTypeModels(List<WLTypeEntity> entity);
//    @InheritInverseConfiguration
//    @Mapping(target = "ID", ignore=true)
//    List<WLTypeEntity> WLTypeModelToWLTypeEntitys(List<WLTypeModel> model);
}
