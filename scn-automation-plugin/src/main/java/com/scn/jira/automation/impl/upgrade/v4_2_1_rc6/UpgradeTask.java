package com.scn.jira.automation.impl.upgrade.v4_2_1_rc6;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.external.ActiveObjectsUpgradeTask;
import com.atlassian.activeobjects.external.ModelVersion;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.scn.jira.automation.impl.upgrade.UpgradeTaskFlow;
import com.scn.jira.common.exception.InternalRuntimeException;
import com.scn.jira.worklog.core.wl.ExtendedConstantsManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDate;

@RequiredArgsConstructor
public class UpgradeTask implements ActiveObjectsUpgradeTask {

    private final ExtendedConstantsManager extendedConstantsManager;
    private final UserManager userManager;

    // ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class).createGlobalSettings().put("AO_DE8E32_#", "3")
    // ComponentAccessor.getOSGiComponentInstanceOfType(PluginSettingsFactory.class).createGlobalSettings().get("AO_DE8E32_#")
    @Override
    public ModelVersion getModelVersion() {
        return UpgradeTaskFlow.V4_2_1_RC6.getModelVersion();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void upgrade(ModelVersion modelVersion, ActiveObjects ao) {
        ao.migrate(AutoTT.class);
        AutoTT[] autoTTList = ao.find(AutoTT.class);
        String worklogTypeId = extendedConstantsManager.getWorklogTypeObjects().stream().findFirst().map(IssueConstant::getId)
            .orElseThrow(() -> new InternalRuntimeException("Empty worklog type list. Please, add worklog type in the config settings."));
        Timestamp startDate = Timestamp.valueOf(LocalDate.now().minusDays(7).atStartOfDay());
        for (AutoTT value : autoTTList) {
            ApplicationUser user = userManager.getUserByKey(value.getUserKey());
            if (user == null) {
                ao.delete(value);
                continue;
            }
            if (value.getStartDate() == null) {
                value.setStartDate(startDate);
            }
            if (StringUtils.isBlank(value.getWorklogTypeId())) {
                value.setWorklogTypeId(worklogTypeId);
            }
            if (StringUtils.isBlank(value.getUsername())) {
                value.setUsername(user.getUsername());
            }
            value.setActive(value.getActive() && user.isActive());
            value.save();
        }
        ao.migrate(com.scn.jira.automation.impl.domain.entity.AutoTT.class);
    }
}
