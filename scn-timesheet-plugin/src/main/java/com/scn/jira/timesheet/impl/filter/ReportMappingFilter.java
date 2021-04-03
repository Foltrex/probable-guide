package com.scn.jira.timesheet.impl.filter;

import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReportMappingFilter implements Filter {
    private final Map<String, String> mappings = new HashMap<>();
    public static final String CONFIGURE_REPORT_ALIAS = "ConfigureReport";

    public ReportMappingFilter() {
        super();
    }

    @Override
    public void init(@Nonnull FilterConfig filterConfig) {
        String mappingsParamValue = filterConfig.getInitParameter("mappings");
        if (!StringUtils.isEmpty(mappingsParamValue)) {
            String[] lines = mappingsParamValue.split(" ");

            for (String line : lines) {
                String[] pairs = line.split("=");
                if (pairs.length != 2) {
                    throw new IllegalArgumentException("Invalid mappings configuration.");
                }

                this.mappings.put(pairs[0], pairs[1]);
            }
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(@Nonnull ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String reportKey = servletRequest.getParameter("reportKey");
        String alias;
        if (request.getParameter("reportOldBehavior") == null && (alias = this.mappings.get(reportKey)) != null) {
            String uri = request.getRequestURI().replace(CONFIGURE_REPORT_ALIAS, alias);
            String q = request.getQueryString();
            response.sendRedirect(StringUtils.isEmpty(q) ? uri : uri + "?" + q);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }
}
