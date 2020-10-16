package com.scn.jira.automation.impl.upgrade.V1;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.external.ActiveObjectsUpgradeTask;
import com.atlassian.activeobjects.external.ModelVersion;
import com.scn.jira.automation.impl.service.AutoTTExecutionService;

public class PluginUpgradeTask01 implements ActiveObjectsUpgradeTask {
    @Override
    public ModelVersion getModelVersion() {
        return ModelVersion.valueOf("1");
    }

    @Override
    public void upgrade(ModelVersion modelVersion, ActiveObjects ao) {
        ao.migrate(AutoTT.class);
        AutoTT[] autoTTList = ao.find(AutoTT.class);
        for (AutoTT record : autoTTList) {
            record.setRatedTime(AutoTTExecutionService.WORKED_TIME);
            record.save();
        }
    }
}
