package com.scn.cloneproject.rest;

import javax.xml.bind.annotation.*;
@XmlRootElement(name = "status")
@XmlAccessorType(XmlAccessType.FIELD)
public class CloneProjectResourceModel {

	@XmlElement
    public String message;
	@XmlElement
    public String pkey;
	@XmlElement
    public String pname;
	@XmlElement
    public String lead;
	@XmlElement
    public String descr;
	@XmlElement
    public String url;
	@XmlElement
    public Long assigneetype;
	@XmlElement
    public String category;
	// user roles ???
	@XmlElement
    public String workflowscheme;
	@XmlElement
    public String issuetypescheme;
	@XmlElement
    public String issuetypesceeenscheme;
	@XmlElement
    public String fieldconfigurationscheme;
	@XmlElement
    public String permissionscheme;
	@XmlElement
    public String notificationscheme;
	@XmlElement
    public String issuesecurityscheme;

    public CloneProjectResourceModel() {
    }
	
	public CloneProjectResourceModel(String message, String pkey, String pname, String lead, String descr, String url,
    		Long assigneetype, String category, String workflowscheme, String issuetypescheme, String issuetypesceeenscheme,
    		String fieldconfigurationscheme, String permissionscheme, String notificationscheme, String issuesecurityscheme) {
        this.message = message;
        this.pkey = pkey;
        this.pname = pname;
        this.lead = lead;
        this.descr = descr;
        this.url = url;
        this.assigneetype = assigneetype;
        this.category= category;
        this.workflowscheme = workflowscheme;
        this.issuetypescheme = issuetypescheme;
        this.issuetypesceeenscheme = issuetypesceeenscheme;
        this.fieldconfigurationscheme = fieldconfigurationscheme;
        this.permissionscheme = permissionscheme;
        this.notificationscheme = notificationscheme;
        this.issuesecurityscheme = issuesecurityscheme;
    }
}
