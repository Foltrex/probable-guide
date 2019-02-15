package com.scn.jira.worklog.wl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ScnResponseWrapper extends HttpServletResponseWrapper
{
	private ByteArrayOutputStream buffer;
	private PrintWriter writer;
	
	public ScnResponseWrapper(HttpServletResponse response)
	{
		super(response);
		
		buffer = new ByteArrayOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(buffer, Charset.forName("UTF-8")));
	}	
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException
	{
		return new ServletOutputStream()
		{
			@Override
			public void write(int b) throws IOException
			{
				buffer.write(b);
			}
		};
	}
	
	@Override
	public PrintWriter getWriter() throws IOException
	{
		return writer;
	}
	
	public String getContent()
	{
		writer.flush();
		return buffer.toString();
	}
	
	public void writeResponse(String content) throws IOException
	{
		super.getOutputStream().write(content.getBytes());
	}
}
