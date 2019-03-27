package com.scn.jira.wl.wltypes.bl;

import java.sql.SQLException;

import com.scn.jira.wl.BLException;
import com.scn.jira.wl.wltypes.dal.WLTypeEntity;
import com.scn.jira.wl.wltypes.service.WLTypeModel;

public interface IWLTypesManager {
	WLTypeEntity[] getAllWLTypes() throws SQLException;
	WLTypeEntity getWLTypeById(int id) throws SQLException;
	WLTypeEntity[] getWLTypesByName(String name) throws SQLException;
	WLTypeEntity addWLType(WLTypeModel dto) throws SQLException, BLException;
	WLTypeEntity editWLType(WLTypeModel dto) throws SQLException, BLException;
	void deleteWLType(int id) throws SQLException;
}
