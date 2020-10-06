package com.scn.jira.automation.impl.servlet;

import com.scn.jira.automation.api.domain.service.ScnBIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Component
public class DebugServlet extends HttpServlet {
    private final ScnBIService scnBIService;

    @Autowired
    public DebugServlet(ScnBIService scnBIService) {
        this.scnBIService = scnBIService;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        try {
            resp.setContentType("text/html");
            resp.getWriter().write("<html><body><pre>");
            resp.getWriter().write("<div>" + scnBIService.getUserCalendar("akalaputs",
                formatter.parse("2020-01-01"),
                formatter.parse("2020-02-01")) + "</div>");
            resp.getWriter().write("</pre></body></html>");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

