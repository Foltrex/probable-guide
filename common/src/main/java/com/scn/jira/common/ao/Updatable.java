package com.scn.jira.common.ao;

import java.sql.Timestamp;

public interface Updatable extends WithId {
    String getAuthorKey();

    void setAuthorKey(String authorKey);

    String getUpdateAuthorKey();

    void setUpdateAuthorKey(String updateAuthorKey);

    Timestamp getCreated();

    void setCreated(Timestamp created);

    Timestamp getUpdated();

    void setUpdated(Timestamp updated);
}
