package com.scn.jira.automation.impl.domain.entity;

import java.sql.Timestamp;
import java.util.Date;

interface Updatable extends WithId {
    String getAuthorKey();

    void setAuthorKey(String authorKey);

    String getUpdateAuthorKey();

    void setUpdateAuthorKey(String updateAuthorKey);

    Timestamp getCreated();

    void setCreated(Date created);

    Timestamp getUpdated();

    void setUpdated(Date updated);
}
