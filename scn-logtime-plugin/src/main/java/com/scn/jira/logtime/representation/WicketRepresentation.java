package com.scn.jira.logtime.representation;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

@Immutable
@XmlRootElement
public class WicketRepresentation {

	@XmlElement
	private String userLogin;

	@XmlElement
	private List<String> times;

	@XmlElement
	private String total;

	public WicketRepresentation() {
	}

	public WicketRepresentation(String userLogin, List<String> times,
			String total) {
		super();
		this.userLogin = userLogin;
		this.times = times;
		this.total = total;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public List<String> getTimes() {
		return times;
	}

	public void setTimes(List<String> times) {
		this.times = times;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

}