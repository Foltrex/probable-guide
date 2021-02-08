package com.scn.jira.worklog.wl;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ScnResponseWrapper extends HttpServletResponseWrapper {
    private final ByteArrayOutputStream buffer;
    private final PrintWriter writer;

    public ScnResponseWrapper(HttpServletResponse response) {
        super(response);

        buffer = new ByteArrayOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) throws IOException {
                buffer.write(b);
            }
        };
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    public String getContent() {
        writer.flush();
        return buffer.toString();
    }

    public void writeResponse(String content) throws IOException {
        super.getOutputStream().write(content.getBytes());
    }
}
