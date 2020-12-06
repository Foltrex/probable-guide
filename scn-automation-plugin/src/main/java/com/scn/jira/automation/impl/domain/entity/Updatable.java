package com.scn.jira.automation.impl.domain.entity;

import java.sql.Timestamp;

interface Updatable extends WithId {
    String getAuthorKey();

    void setAuthorKey(String authorKey);

    String getUpdateAuthorKey();

    void setUpdateAuthorKey(String updateAuthorKey);

    Timestamp getCreated();

    void setCreated(Timestamp created);

    Timestamp getUpdated();

    void setUpdated(Timestamp updated);
}
