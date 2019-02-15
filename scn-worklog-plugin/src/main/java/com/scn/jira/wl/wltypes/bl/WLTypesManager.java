package com.scn.jira.wl.wltypes.bl;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;

import com.scn.jira.wl.BLException;
import com.scn.jira.wl.wltypes.dal.IWLTypesStore;
import com.scn.jira.wl.wltypes.dal.WLTypeEntity;
import com.scn.jira.wl.wltypes.service.WLTypeModel;

@Named("WLTypesManager")
public class WLTypesManager implements IWLTypesManager {
	private final IWLTypesStore store;

	@Inject
	public WLTypesManager(IWLTypesStore store) {
		this.store = store;
	}

	@Override
	public WLTypeEntity[] getAllWLTypes() throws SQLException {
		return store.getAllWLTypes();
	}

	@Override
	public WLTypeEntity getWLTypeById(int id) throws SQLException {
		return store.getWLTypeById(id);
	}

	@Override
	public WLTypeEntity[] getWLTypesByName(String name) throws SQLException {
		return store.getWLTypesByName(name);
	}

	@Override
	public WLTypeEntity addWLType(WLTypeModel dto) throws SQLException, BLException {
		if (dto.name == null) {
			throw new BLException("WLType.name cannot be null");
		}
		if (getWLTypesByName(dto.name).length > 0) {
			throw new BLException("WLType with the same name already exists");
		}
		return store.addWLType(dto);
	}

	@Override
	public WLTypeEntity editWLType(WLTypeModel dto) throws SQLException, BLException {
		if (dto.id <= 0) {
			throw new BLException("WLType.ID cannot be <= 0");
		}
		if (dto.name == null) {
			throw new BLException("WLType.name cannot be null");
		}
		return store.editWLType(dto);
	}

	@Override
	public void deleteWLType(int id) throws SQLException {
		store.deleteWLType(store.getWLTypeById(id));
	}

}
