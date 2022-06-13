package com.scn.jira.automation.impl.upgrade.v2;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.external.ActiveObjectsUpgradeTask;
import com.atlassian.activeobjects.external.ModelVersion;

import java.sql.Timestamp;
import java.time.LocalDate;

public class PluginUpgradeTask02 implements ActiveObjectsUpgradeTask {
    @Override
    public ModelVersion getModelVersion() {
        return ModelVersion.valueOf("2");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void upgrade(ModelVersion modelVersion, ActiveObjects ao) {
        ao.migrate(AutoTT.class);
        AutoTT[] autoTTList = ao.find(AutoTT.class);
        Timestamp startDate = Timestamp.valueOf(LocalDate.now().minusDays(7).atStartOfDay());
        for (AutoTT value : autoTTList) {
            value.setStartDate(startDate);
            value.save();
        }
    }
}
