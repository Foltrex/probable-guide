package com.scn.jira.worklog.wl;

import static com.atlassian.jira.issue.fields.TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE;
import static com.atlassian.jira.issue.fields.TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE;
import static com.scn.jira.worklog.customfield.ScnTimeTrackingType.ORIGINAL_ESTIMATE;
import static com.scn.jira.worklog.customfield.ScnTimeTrackingType.REMAINING_ESTIMATE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.atlassian.jira.issue.fields.CustomField;

public class IssueActionFilter extends BaseFilter
{
	private class ContentItem
	{
		private byte[] headers;
		private byte[] body;
		
		public ContentItem(byte[] headers, byte[] body)
		{
			super();
			this.headers = headers;
			this.body = body;
		}
		
		public byte[] getHeaders()
		{
			return headers;
		}
		
		public void setHeaders(byte[] headers)
		{
			this.headers = headers;
		}
		
		public byte[] getBody()
		{
			return body;
		}
		
		public void setBody(byte[] body)
		{
			this.body = body;
		}
	}

	private static final Logger log = Logger.getLogger(IssueActionFilter.class);
	
	@Override
	protected void overrideRequest(ScnRequestWrapper request, ScnResponseWrapper response)
	{
		if (ServletFileUpload.isMultipartContent(request))
		{
			try
			{
				Map<String, ContentItem> contentParams = parseMultipartContent(request);
				Map<String, String[]> requestParams = convertToRequestParams(contentParams);
				
				if (shouldOverrideRequest(requestParams))
				{
					overrideMultipartRequest(contentParams);
					ByteArrayOutputStream content = buildMultipartContent(contentParams, getBoundary(request));
					request.setContent(content);
				}
			}
			catch (Exception e)
			{
				log.error("Filter couldn't process multipart request. Action executes without parameters overriding.", e);
			}
		}
		else
		{
			Map<String, String[]> requestParams = request.getParameterMap();
			
			if (shouldOverrideRequest(requestParams))
			{
				requestParams = overrideParams(requestParams);
				request.setParameterMap(requestParams);
			}
		}
	}

	@Override
	protected void overrideResponse(ScnRequestWrapper request, ScnResponseWrapper response) throws IOException
	{
		String content = response.getContent();
		
		if (shouldOverrideResponse(request.getParameterMap(), content))
		{
			content = overrideHTML(content);
		}
		
		response.writeResponse(content);
	}
	
	private void overrideMultipartRequest(Map<String, ContentItem> contentParams)
	{
		CustomField customField = getTimetrackingCustomField();
		
		ContentItem customOriginalEstimateItem = contentParams.get(customField.getId() + ":" + ORIGINAL_ESTIMATE);
		ContentItem systemOriginalEstimateItem = new ContentItem(
					createHeaderFor(TIMETRACKING_ORIGINALESTIMATE),
					customOriginalEstimateItem.getBody());
		contentParams.put(TIMETRACKING_ORIGINALESTIMATE, systemOriginalEstimateItem);
				
		ContentItem customRemainingEstimateItem = contentParams.get(customField.getId() + ":" + REMAINING_ESTIMATE);
		ContentItem systemRemainingEstimateItem = new ContentItem(
					createHeaderFor(TIMETRACKING_REMAININGESTIMATE),
					customRemainingEstimateItem.getBody());
		contentParams.put(TIMETRACKING_REMAININGESTIMATE, systemRemainingEstimateItem);
	}
	
	private String overrideHTML(String content)
	{
		Document doc = Jsoup.parse(content);
		doc.select("div.field-group:has(input[id^=timetracking])").remove();
		
		return doc.toString();
	}
	
	private Map<String, ContentItem> parseMultipartContent(HttpServletRequest request) throws IOException, MultipartStream.MalformedStreamException
    {
        Map<String, ContentItem> contentParams = new LinkedHashMap<String, ContentItem>();

        MultipartStream stream = new MultipartStream(request.getInputStream(), getBoundary(request).getBytes());
        for(boolean nextPart = stream.skipPreamble(); nextPart; nextPart = stream.readBoundary())
        {
        	String headers = stream.readHeaders();
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            stream.readBodyData(body);
            
            contentParams.put(getFieldName(headers), new ContentItem(headers.getBytes(), body.toByteArray()));
        }

        return contentParams;
    }
	
	private Map<String, String[]> convertToRequestParams(Map<String, ContentItem> contentParams)
	{
		Map<String, String[]> params = new HashMap<String, String[]>();
		
		for (String fieldName : contentParams.keySet())
		{
			ContentItem item = contentParams.get(fieldName);
			byte[] body = item.getBody();
			// skip field if body to large
			if (body.length < 4096)
			{
				String value = (new String(body)).trim();
				params.put(fieldName, new String[] {StringUtils.isNotBlank(value) ? value : null});
			}
		}
		
		return params;
	}
	
	private ByteArrayOutputStream buildMultipartContent(Map<String, ContentItem> contentParams, String boundary) throws IOException
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		for (String fieldName : contentParams.keySet())
		{
			ContentItem item = contentParams.get(fieldName);
			
			os.write(("--" + boundary + "\r\n").getBytes());
			os.write(item.getHeaders());
			os.write(item.getBody());
			os.write("\r\n".getBytes());
		}
		os.write(("--" + boundary + "--\r\n").getBytes());
		os.flush();
		
		return os;
	}
	
	private String getFieldName(String headers)
	{
		String contentDisposition = "Content-Disposition: form-data;";
		int contentDispositionIndex = headers.indexOf("Content-Disposition: form-data;");
		if (contentDispositionIndex == -1) return null;
		
		int fieldNameStartIndex = headers.indexOf("name=\"", contentDispositionIndex + contentDisposition.length()) + "name=\"".length();
		int fieldNameEndIndex = headers.indexOf("\"", fieldNameStartIndex);
		
        return headers.substring(fieldNameStartIndex, fieldNameEndIndex);
	}
	
	private byte[] createHeaderFor(String fieldName)
	{
		return ("Content-Disposition: form-data; name=\"" + TIMETRACKING_REMAININGESTIMATE + "\"\r\n").getBytes();
	}
	
	private String getBoundary(HttpServletRequest request)
	{
		String contentType = request.getContentType();
		return contentType.substring(contentType.indexOf("boundary=") + "boundary=".length());
	}
}