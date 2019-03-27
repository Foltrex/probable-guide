package com.scn.jira.worklog.wl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

public class ScnRequestWrapper extends HttpServletRequestWrapper
{
	private ByteArrayOutputStream buffer;
	private Map<String, String[]> params;
		
	public ScnRequestWrapper(HttpServletRequest request) throws IOException
	{
		super(request);
		
		buffer = new ByteArrayOutputStream();
		IOUtils.copy(request.getInputStream(), buffer);
        buffer.flush();
        
        params = new HashMap<String, String[]>(request.getParameterMap());
	}	
	
	@Override
	public ServletInputStream getInputStream() throws IOException
	{
		return new ServletInputStream() 
		{
			private ByteArrayInputStream is = new ByteArrayInputStream(buffer.toByteArray());
			
			@Override
            public int read() throws IOException
            {
                return is.read();
            }
        };
	}
	
	@Override
	public BufferedReader getReader() throws IOException
	{
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}
	
	@Override
	public int getContentLength()
	{
		return buffer.size();
	}
	
	public void setContent(ByteArrayOutputStream content) throws IOException
	{
		buffer = content;
	}
	
	@Override
	public Map<String, String[]> getParameterMap()
	{
		return params;
	}
	
	@Override
	public String getParameter(String name)
	{
		String[] value = params.get(name);

		if (value != null && value.length > 0)
			return value[0];
		else 
			return null;
	}
	
	@Override
	public Enumeration getParameterNames()
	{
		return new Vector(params.keySet()).elements();
	}
	
	@Override
	public String[] getParameterValues(String name)
	{
		return params.get(name);
	}
	
	public void setParameterMap(Map<String, String[]> params)
	{
		this.params = params;
	}
	
	public void setParameterValues(String key, String[] values)
	{
		this.params.put(key, values);
	}
	
	public void setParameter(String key, String value)
	{
		this.params.put(key, new String[] {value});
	}
}
