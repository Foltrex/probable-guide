package com.scn.jira.automation.impl.upgrade;

import com.atlassian.activeobjects.external.ModelVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UpgradeTaskFlow {
    V4_2_1_RC6(ModelVersion.valueOf("4"));

    private final ModelVersion modelVersion;
}
