package com.scn.jira.worklog.wltypes.dal;

import net.java.ao.Entity;
import net.java.ao.schema.*;

public interface WLTypeEntity extends Entity {
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
