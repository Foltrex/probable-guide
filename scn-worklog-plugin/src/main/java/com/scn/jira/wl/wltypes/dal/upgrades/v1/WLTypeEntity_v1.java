package com.scn.jira.wl.wltypes.dal.upgrades.v1;

import net.java.ao.Entity;
import net.java.ao.schema.*;

@Table("WLTypeEntity")
public interface WLTypeEntity_v1 extends Entity {
	@NotNull
	@Unique
	public String getName();
	public void setName(String name);

	// needed once for data migration only, in next version - kill
	public int getOldID();
	public void setOldID(int id);

	public int getSequence();
	public void setSequence(int sequence);

	public String getDescription();
	public void setDescription(String description);

	public String getIconUri();
	public void setIconUri(String icon);
}
