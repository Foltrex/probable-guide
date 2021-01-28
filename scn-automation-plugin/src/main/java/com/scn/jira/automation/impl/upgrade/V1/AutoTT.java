package com.scn.jira.automation.impl.upgrade.V1;

import net.java.ao.Preload;

@Preload
public interface AutoTT extends com.scn.jira.automation.impl.domain.entity.AutoTT {
    Long getRatedTime();

    void setRatedTime(Long ratedTime);
}
