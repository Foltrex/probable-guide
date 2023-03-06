package com.scn.jira.common.ao;

import java.sql.Timestamp;
import net.java.ao.schema.NotNull;

public interface Updatable extends WithId {

    @NotNull
    String getAuthorKey();

    void setAuthorKey(String authorKey);

    @NotNull
    String getUpdateAuthorKey();

    void setUpdateAuthorKey(String updateAuthorKey);

    @NotNull
    Timestamp getCreated();

    void setCreated(Timestamp created);

    @NotNull
    Timestamp getUpdated();

    void setUpdated(Timestamp updated);
}
