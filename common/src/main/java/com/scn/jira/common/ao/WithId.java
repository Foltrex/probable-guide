package com.scn.jira.common.ao;

import net.java.ao.RawEntity;
import net.java.ao.schema.AutoIncrement;
import net.java.ao.schema.PrimaryKey;

public interface WithId extends RawEntity<Long> {

    String ID = "ID";

    @AutoIncrement
    @PrimaryKey(ID)
    Long getId();
}
