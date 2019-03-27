package com.scn.jira.mytime.representation;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

/**
 * JAXB representation of a project's information. This can be marshalled as
 * either JSON or XML, depending on what the client asks for.
 */
@Immutable
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class WeekRepresentation {
	
	@XmlElement
	private String totalWicket;
	
	@XmlElement
	private String total;
	
	@XmlElement
	private List<DayRepresentation> daysRepresentation;
	
	@XmlElement
	private String cssClassWicketTotal;
	
	@XmlElement
	private String cssClassWlTotal;
	
	public String getTotalWicket() {
		return totalWicket;
	}
	
	public void setTotalWicket(String totalWicket) {
		this.totalWicket = totalWicket;
	}
	
	public String getTotal() {
		return total;
	}
	
	public void setTotal(String total) {
		this.total = total;
	}
	
	public List<DayRepresentation> getDaysRepresentation() {
		return daysRepresentation;
	}
	
	public void setDaysRepresentation(List<DayRepresentation> daysRepresentation) {
		this.daysRepresentation = daysRepresentation;
	}
	
	public String getCssClassWicketTotal() {
		return cssClassWicketTotal;
	}
	
	public void setCssClassWicketTotal(String cssClassWicketTotal) {
		this.cssClassWicketTotal = cssClassWicketTotal;
	}
	
	public String getCssClassWlTotal() {
		return cssClassWlTotal;
	}
	
	public void setCssClassWlTotal(String cssClassWlTotal) {
		this.cssClassWlTotal = cssClassWlTotal;
	}
	
}