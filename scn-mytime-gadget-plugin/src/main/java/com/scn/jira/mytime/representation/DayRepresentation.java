package com.scn.jira.mytime.representation;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

/**
 * JAXB representation of a project's information. This can be marshalled as
 * either JSON or XML, depending on what the client asks for.
 */
@Immutable
@XmlRootElement
public class DayRepresentation {
	@XmlElement
	private String dateString;

	@XmlElement
	private Date date;

	@XmlElement
	private String day;

	@XmlElement
	private String dayColor;

	@XmlElement
	private Long timelong;

	@XmlElement
	private Long wicketTimeLong;

	@XmlElement
	private String time;

	@XmlElement
	private String wicketTime;

	@XmlElement
	private String cssClassWicket;

	@XmlElement
	private String cssClassWl;

	@XmlElement
	private Boolean month;

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

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public Long getTimelong() {
		return timelong;
	}

	public void setTimelong(Long timelong) {
		this.timelong = timelong;
	}

	public Long getWicketTimeLong() {
		return wicketTimeLong;
	}

	public void setWicketTimeLong(Long wicketTimeLong) {
		this.wicketTimeLong = wicketTimeLong;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getWicketTime() {
		return wicketTime;
	}

	public void setWicketTime(String wicketTime) {
		this.wicketTime = wicketTime;
	}

	public String getCssClassWicket() {
		return cssClassWicket;
	}

	public void setCssClassWicket(String cssClassWicket) {
		this.cssClassWicket = cssClassWicket;
	}

	public String getCssClassWl() {
		return cssClassWl;
	}

	public void setCssClassWl(String cssClassWl) {
		this.cssClassWl = cssClassWl;
	}

	public Boolean getMonth() {
		return month;
	}

	public void setMonth(Boolean month) {
		this.month = month;
	}

	public String getDayColor() {
		return dayColor;
	}

	public void setDayColor(String dayColor) {
		this.dayColor = dayColor;
	}
}