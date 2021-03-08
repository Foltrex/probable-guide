package com.scn.jira.worklog.wltypes.bl;

import com.scn.jira.worklog.wl.BLException;
import com.scn.jira.worklog.wltypes.dal.WLTypeEntity;
import com.scn.jira.worklog.wltypes.service.WLTypeModel;

import java.sql.SQLException;

public interface IWLTypesManager {
    WLTypeEntity[] getAllWLTypes() throws SQLException;

    WLTypeEntity getWLTypeById(int id) throws SQLException;

    WLTypeEntity[] getWLTypesByName(String name) throws SQLException;

    WLTypeEntity addWLType(WLTypeModel dto) throws SQLException, BLException;

    WLTypeEntity editWLType(WLTypeModel dto) throws SQLException, BLException;

    void deleteWLType(int id) throws SQLException;
}
