package com.scn.jira.wl.wltypes.dal.upgrades.v1;

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.external.ActiveObjectsUpgradeTask;
import com.atlassian.activeobjects.external.ModelVersion;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import com.scn.jira.worklog.core.wl.WorklogType;

/*
 * the upgrade is about the very first data migration from OfBiz's Entity Engine ORM data model to the Active Objects ORM
 */
public final class Upgrade001 implements ActiveObjectsUpgradeTask {

	private static final Logger log = LogManager.getLogger(Upgrade001.class);
	private final ExtendedConstantsManager oldStore;

	public Upgrade001(ExtendedConstantsManager oldStore) {
		this.oldStore = oldStore;
	}

	@Override
	public ModelVersion getModelVersion() {
		return ModelVersion.valueOf("1");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void upgrade(ModelVersion currentVersion, ActiveObjects ao) {
		log.warn(">>>>>>>>>>>>>>>>>>>>>>> AO UPGRADE WLTYPES 001. START <<<<<<<<<<<<<<<<<<<<. VERSION: from " + currentVersion.toString() + " into " + getModelVersion().toString());
		try {
			// init AO upgrading entities
			ao.migrate(WLTypeEntity_v1.class);

			// get all configuration
			Collection<WorklogType> oldConfig = oldStore.getWorklogTypeObjects();
			// transform into AO entities
			for (WorklogType oldCfg : oldConfig) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("NAME", oldCfg.getName());
				map.put("OLD_ID", Integer.parseInt(oldCfg.getId()));
				map.put("SEQUENCE", oldCfg.getSequence());
				map.put("DESCRIPTION", oldCfg.getDescription());
				map.put("ICON_URI", oldCfg.getIconUrl());
				WLTypeEntity_v1 entity = ao.create(WLTypeEntity_v1.class, map);
				entity.save();
			}
			log.warn(">>>>>>>>>>>>>>>>>>>>>>> AO UPGRADE WLTYPES 001. FINISH SUCCESSFULLY <<<<<<<<<<<<<<<<<<<<");
		} catch(Exception e) {
			log.error(">>>>>>>>>>>>>>>>>>>>>>> AO UPGRADE WLTYPES 001. FAILED <<<<<<<<<<<<<<<<<<<<", e);
		}
	}
}
