package com.scn.jira.plugin.gadget.rest.error;

import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement
public class ErrorCollection {

	@XmlElement
	private Collection<String> errorMessages = new ArrayList<String>();

	@XmlElement
	private Collection<ValidationError> errors = new ArrayList<ValidationError>();

	private ErrorCollection() {
	}

	private ErrorCollection(Collection<ValidationError> errors, Collection<String> errorMessages) {
		Assertions.notNull("errors", errors);
		Assertions.notNull("errorMessages", errorMessages);

		this.errorMessages.addAll(errorMessages);
		this.errors.addAll(errors);
	}

	private void addValidationError(ValidationError validationError) {
		this.errors.add(validationError);
	}

	public boolean hasAnyErrors() {
		return (!this.errorMessages.isEmpty()) || (!this.errors.isEmpty());
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

	public static class Builder {
		private ErrorCollection errorCollection;

		public static Builder newBuilder() {
			return new Builder(Collections.<ValidationError>emptyList(), Collections.<String>emptyList());
		}

		Builder(Collection<ValidationError> errors, Collection<String> errorMessages) {
			this.errorCollection = new ErrorCollection(errors, errorMessages);
		}

		public Builder addError(String field, String errorKey, String[] params) {
			Assertions.notNull("field", field);
			Assertions.notNull("errorKey", errorKey);

			if ((params != null) && (params.length > 0)) {
				this.errorCollection.addValidationError(new ValidationError(field, errorKey, Arrays.asList(params)));
			} else {
				this.errorCollection.addValidationError(new ValidationError(field, errorKey));
			}
			return this;
		}

		public ErrorCollection build() {
			return this.errorCollection;
		}
	}
}