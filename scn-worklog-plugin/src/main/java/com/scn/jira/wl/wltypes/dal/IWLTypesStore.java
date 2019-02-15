package com.scn.jira.wl.wltypes.dal;

import java.sql.SQLException;

import com.atlassian.activeobjects.tx.Transactional;
import com.scn.jira.wl.wltypes.service.WLTypeModel;

@Transactional //!!! every method proceed in AO transaction
public interface IWLTypesStore {
	WLTypeEntity[] getAllWLTypes() throws SQLException;
	WLTypeEntity getWLTypeById(int id) throws SQLException;
	WLTypeEntity[] getWLTypesByName(String name) throws SQLException;
	WLTypeEntity addWLType(WLTypeModel dto) throws SQLException;
	WLTypeEntity editWLType(WLTypeModel dto) throws SQLException;
	void deleteWLType(WLTypeEntity entity) throws SQLException;
}
