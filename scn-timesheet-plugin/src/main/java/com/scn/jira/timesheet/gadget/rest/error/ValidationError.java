package com.scn.jira.timesheet.gadget.rest.error;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

@XmlRootElement
public class ValidationError {

	@XmlElement
	private String field;

	@XmlElement
	private String error;

	@XmlElement
	private List<String> params;

	public ValidationError() {
	}

	public ValidationError(String field, String error) {
		this.field = field;
		this.error = error;
	}

	public ValidationError(String field, String error, List<String> params) {
		this.field = field;
		this.error = error;
		this.params = params;
	}

	public ValidationError(String field, String error, String param) {
		this(field, error, Arrays.asList(new String[] { param }));
	}

	public String getField() {
		return this.field;
	}

	public String getError() {
		return this.error;
	}

	public List<String> getParams() {
		return this.params;
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
