package com.scn.jira.worklog.wltypes.dal;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.scn.jira.worklog.wltypes.service.WLTypeModel;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.SQLException;
import java.util.HashMap;

@Named("WLTypesStore")
public class WLTypesStore implements IWLTypesStore {
    private final ActiveObjects ao;

    @Inject
    public WLTypesStore(final ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public WLTypeEntity[] getAllWLTypes() throws SQLException {
        return ao.find(WLTypeEntity.class);
    }

    @Override
    public WLTypeEntity getWLTypeById(int id) throws SQLException {
        return ao.get(WLTypeEntity.class, id);
    }

    @Override
    public WLTypeEntity[] getWLTypesByName(String name) throws SQLException {
        return ao.find(WLTypeEntity.class, "NAME = ?", name);
    }

    @Override
    public WLTypeEntity addWLType(WLTypeModel dto) throws SQLException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("NAME", dto.getName());
        map.put("DESCRIPTION", dto.getDescription());
        map.put("ICON_URI", dto.getIconUri());
        map.put("SEQUENCE", dto.getSequence());
        WLTypeEntity row = ao.create(WLTypeEntity.class, map);
        row.save();
        return row;
    }

    @Override
    public WLTypeEntity editWLType(WLTypeModel dto) throws SQLException {
        WLTypeEntity row = getWLTypeById(dto.getId());
        row.setName(dto.getName());
        row.setDescription(dto.getDescription());
        row.setIconUri(dto.getIconUri());
        row.setSequence(dto.getSequence());
        row.save();
        return row;
    }

    @Override
    public void deleteWLType(WLTypeEntity entity) throws SQLException {
        ao.delete(entity);
    }
}
