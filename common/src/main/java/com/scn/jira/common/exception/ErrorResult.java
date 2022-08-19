package com.scn.jira.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.annotation.Nonnull;
import java.io.Serializable;

@NoArgsConstructor
@Getter
@JsonAutoDetect
public class ErrorResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String errorMessage;
    private String stackTrace;

    public ErrorResult(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ErrorResult(@Nonnull Throwable e) {
        this.errorMessage = StringUtils.isNotBlank(e.getLocalizedMessage()) ? e.getLocalizedMessage() : e.getClass().toString();
        this.stackTrace = ExceptionUtils.getStackTrace(e);
    }
}
