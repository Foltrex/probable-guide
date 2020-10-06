package com.scn.jira.automation.impl.domain.entity;

import net.java.ao.RawEntity;
import net.java.ao.schema.AutoIncrement;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.PrimaryKey;

interface WithId extends RawEntity<Long> {
    String ID = "id";

    @AutoIncrement
    @NotNull
    @PrimaryKey("id")
    long getId();
}
