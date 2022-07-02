package com.scn.jira.automation.impl.servlet;

import com.scn.jira.automation.api.domain.service.ScnBIService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DebugServlet extends HttpServlet {
    private final ScnBIService scnBIService;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.getWriter().write("<html><body><pre>");
        resp.getWriter().write("<div>" + scnBIService.getUserCalendar("akalaputs",
            LocalDate.parse("2020-01-01"),
            LocalDate.parse("2020-02-01")) + "</div>");
        resp.getWriter().write("</pre></body></html>");
    }
}

