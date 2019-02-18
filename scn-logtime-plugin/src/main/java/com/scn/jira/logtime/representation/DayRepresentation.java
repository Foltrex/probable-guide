package com.scn.jira.logtime.representation;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

@Immutable
@XmlRootElement
public class DayRepresentation {

	@XmlElement
	private String dateString;

	@XmlElement
	private Date date;

	@XmlElement
	private String weekNumber;

	@XmlElement
	private String cssClass;

	public DayRepresentation(String dateString, Date date, String cssClass,
			String weekNumber) {
		super();
		this.dateString = dateString;
		this.date = date;
		this.cssClass = cssClass;
		this.weekNumber = weekNumber;
	}

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public String getWeekNumber() {
		return weekNumber;
	}

	public void setWeekNumber(String weekNumber) {
		this.weekNumber = weekNumber;
	}

}