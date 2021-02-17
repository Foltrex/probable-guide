package com.scn.jira.worklog.wl;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ScnRequestWrapper extends HttpServletRequestWrapper {
    private ByteArrayOutputStream buffer;
    private Map<String, String[]> params;

    public ScnRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        buffer = new ByteArrayOutputStream();
        IOUtils.copy(request.getInputStream(), buffer);
        buffer.flush();

        params = new HashMap<>(request.getParameterMap());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return true;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            private ByteArrayInputStream is = new ByteArrayInputStream(buffer.toByteArray());

            @Override
            public int read() throws IOException {
                return is.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public int getContentLength() {
        return buffer.size();
    }

    public void setContent(ByteArrayOutputStream content) throws IOException {
        buffer = content;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return params;
    }

    @Override
    public String getParameter(String name) {
        String[] value = params.get(name);

        if (value != null && value.length > 0)
            return value[0];
        else
            return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new Vector<>(params.keySet()).elements();
    }

    @Override
    public String[] getParameterValues(String name) {
        return params.get(name);
    }

    public void setParameterMap(Map<String, String[]> params) {
        this.params = params;
    }

    public void setParameterValues(String key, String[] values) {
        this.params.put(key, values);
    }

    public void setParameter(String key, String value) {
        this.params.put(key, new String[]{value});
    }
}
