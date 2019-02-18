package com.scn.jira.logtime.representation;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

import com.scn.jira.logtime.util.DateUtils;
import com.scn.jira.logtime.util.TextFormatUtil;

import net.jcip.annotations.Immutable;

@Immutable
@XmlRootElement
public class WLRepresentation
{
    @XmlElement
    private Long id;
   
    @XmlElement
    private String date;
 
    @XmlElement
    private Long linkedId;
    
    @XmlElement
    private String wlTypeId;

    @XmlElement
    private String wlTypeName;

    @XmlElement
    private Boolean isExt;
    
    @XmlElement
    private String comment;
    
    @XmlElement
    private String timeSpent;
    
    @XmlElement
    private String timeSpentString;
    
    @XmlElement
    private String timeSpentString2;    
    
    
    @XmlElement
    private String dayColor;

    
    // This private constructor isn't used by any code, but JAXB requires any
    // representation class to have a no-args constructor.
    public WLRepresentation()
    {
    	 this.id = new Long(0);
    	 this.linkedId = null;
         this.wlTypeId = null;
         this.wlTypeName = null;
         this.comment="";
         this.isExt=null;
         this.timeSpent="0";
         this.timeSpentString="00:00";
         this.timeSpentString2="";
         this.dayColor="";
         
         
    }
    
    public WLRepresentation(String date, Integer status)
    {
    	 this();
    	 this.date = date; 
    	 this.dayColor= DateUtils.getDayColor(date,status);
    }

	public WLRepresentation(Long id, Long linkedId,
			String wlTypeId, String wlTypeName, Boolean isExt) {
		super();
		this.id = id;		
		this.linkedId = linkedId;
		this.wlTypeId = wlTypeId;
		this.wlTypeName = wlTypeName;
		this.isExt = isExt;
		this.comment="";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLinkedId() {
		return linkedId;
	}

	public void setLinkedId(Long linkedId) {
		this.linkedId = linkedId;
	}

	public String getWlTypeId() {
		return wlTypeId;
	}

	public void setWlTypeId(String wlTypeId) {
		this.wlTypeId = wlTypeId;
	}

	public String getWlTypeName() {
		return wlTypeName;
	}

	public void setWlTypeName(String wlTypeName) {
		this.wlTypeName = wlTypeName;
	}

	public Boolean getIsExt() {
		return isExt;
	}

	public void setIsExt(Boolean isExt) {
		this.isExt = isExt;
	}

	public String getTimeSpent() {
		return timeSpent;
	}

	public void setTimeSpent(String timeSpent) {
		this.timeSpent = timeSpent;
	}

	public String getTimeSpentString() {
		return timeSpentString;
	}

	public void setTimeSpentString(String timeSpentString) {
		this.timeSpentString = timeSpentString;
	}

	
	public String getTimeSpentString2() {
		return timeSpentString2;
	}

	public void setTimeSpentString2(String timeSpentString2) {
		this.timeSpentString2 = timeSpentString2;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		
		this.comment = TextFormatUtil.replaceHTMLSymbols(comment);
		
	}

	public String getDayColor() {
		return dayColor;
	}

	public void setDayColor(String dayColor) {
		this.dayColor = dayColor;
	}   
	
	
	
	
    
	
    
}