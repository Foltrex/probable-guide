package com.scn.jira.worklog.wltypes.dal.upgrades.v1;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;

@Table("WLTypeEntity")
public interface WLTypeEntity_v1 extends Entity {
    @NotNull
    @Unique
    String getName();

    void setName(String name);

    // needed once for data migration only, in next version - kill
    int getOldID();

    void setOldID(int id);

    int getSequence();

    void setSequence(int sequence);

    String getDescription();

    void setDescription(String description);

    String getIconUri();

    void setIconUri(String icon);
}
