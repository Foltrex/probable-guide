package com.scn.jira.worklog.impl.domain.entity;

import com.scn.jira.common.ao.WithId;
import net.java.ao.schema.*;

public interface WLType extends WithId {

    @Override
    @PrimaryKey(ID)
    Long getId();

    @NotNull
    @Unique
    @StringLength(60)
    String getName();

    void setName(String name);

    int getSequence();

    void setSequence(int sequence);

    @StringLength(StringLength.UNLIMITED)
    String getDescription();

    void setDescription(String description);

    @StringLength(255)
    String getIconUri();

    void setIconUri(String icon);

    @StringLength(60)
    String getStatusColor();

    void setStatusColor(String icon);
}
