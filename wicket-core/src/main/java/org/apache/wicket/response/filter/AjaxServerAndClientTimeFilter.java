/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.response.filter;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.apache.wicket.util.value.AttributeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a filter that injects javascript code to the top head portion and after the body so that
 * the time can me measured what the client parse time was for this page. It also reports the total
 * server parse/response time in the client and logs the server response time and response size it
 * took for a specific response in the server log.
 * 
 * You can specify what the status text should be like this: ServerAndClientTimeFilter.statustext=My
 * Application, Server parsetime: ${servertime}, Client parsetime: ${clienttime} likewise for ajax
 * request use ajax.ServerAndClientTimeFilter.statustext
 * 
 * <p>
 * Usage: in YourApplication.java:
 * 
 * <pre>
 * &#064;Override
 * public init()
 * {
 * 	super.init();
 * 	getRequestCycleSettings().addResponseFilter(new AjaxServerAndClientTimeFilter());
 * }
 * </pre>
 * 
 * @author jcompagner
 */
public class AjaxServerAndClientTimeFilter implements IResponseFilter
{
	private static final String HEADER_CONTRIBUTION_START = "<header-contribution><![CDATA[<head xmlns:wicket=\"http://wicket.apache.org\">";
	private static final String HEADER_CONTRIBUTION_END = "</head>]]></header-contribution>";
	public static final String CDATA_SCRIPT_END = "/*]]]]><![CDATA[>*/</script>";

	private static Logger log = LoggerFactory.getLogger(AjaxServerAndClientTimeFilter.class);

	/**
	 * @see IResponseFilter#filter(org.apache.wicket.util.string.AppendingStringBuffer)
	 */
	@Override
	public AppendingStringBuffer filter(AppendingStringBuffer responseBuffer)
	{
		int headIndex = responseBuffer.indexOf("<head>");
		int bodyIndex = responseBuffer.indexOf("</body>");
		int ajaxStart = responseBuffer.indexOf("<ajax-response>");
		int ajaxEnd = responseBuffer.indexOf("</ajax-response>");
		long timeTaken = System.currentTimeMillis() - RequestCycle.get().getStartTime();
		String nonce = getNonce();
		boolean hasBody = headIndex != -1 && bodyIndex != -1;
		boolean hasAjaxResponse = ajaxStart != -1 && ajaxEnd != -1;

		if (hasBody || hasAjaxResponse)
		{
			AppendingStringBuffer startScript = new AppendingStringBuffer(250);
			startScript.append(createScriptOpenTag(false, nonce));
			startScript.append(JavaScriptUtils.SCRIPT_CONTENT_PREFIX);
			startScript.append("clientTimeVariable = new Date().getTime();");
			startScript.append(CDATA_SCRIPT_END);
			AppendingStringBuffer endScript = new AppendingStringBuffer(300);
			endScript.append(createScriptOpenTag(true, nonce));
			endScript.append(JavaScriptUtils.SCRIPT_CONTENT_PREFIX);
			endScript.append("\nwindow.defaultStatus='" + getStatusString(timeTaken, "ajax.ServerAndClientTimeFilter.statustext"));
			endScript.append(CDATA_SCRIPT_END);

			if (hasBody)
			{
				responseBuffer.insert(bodyIndex, endScript);
				responseBuffer.insert(headIndex + 6, startScript);
			}
			else
			{
				AppendingStringBuffer headerContributionStartBuffer = new AppendingStringBuffer(500);
				headerContributionStartBuffer.append(HEADER_CONTRIBUTION_START);
				headerContributionStartBuffer.append(startScript);
				headerContributionStartBuffer.append(HEADER_CONTRIBUTION_END);
				AppendingStringBuffer headerContributionEndBuffer = new AppendingStringBuffer(500);
				headerContributionEndBuffer.append(HEADER_CONTRIBUTION_START);
				headerContributionEndBuffer.append(endScript);
				headerContributionEndBuffer.append(HEADER_CONTRIBUTION_END);
				responseBuffer.insert(ajaxEnd, headerContributionEndBuffer);
				responseBuffer.insert(ajaxStart + 15, headerContributionStartBuffer);
			}
		}
		log.info(timeTaken + "ms server time taken for request " +
			RequestCycle.get().getRequest().getUrl() + " response size: " + responseBuffer.length());
		return responseBuffer;
	}

	private String createScriptOpenTag(boolean isAfterDomRender, String nonce)
	{
		AttributeMap attrs = new AttributeMap();
		attrs.putAttribute(JavaScriptUtils.ATTR_TYPE, "text/javascript");
		if (isAfterDomRender)
		{
			attrs.putAttribute("data-wicket-evaluation", "after");
		}
		if (nonce != null)
		{
			attrs.putAttribute(JavaScriptUtils.ATTR_CSP_NONCE, "nonce");
		}
		return "<script" + attrs.toString() + ">";
	}

	/**
	 * Returns a locale specific status message about the server and client time.
	 * 
	 * @param timeTaken
	 *            the server time it took
	 * @param resourceKey
	 *            The key for the locale specific string lookup
	 * @return String with the status message
	 */
	private String getStatusString(long timeTaken, String resourceKey)
	{
		final String txt = Application.get()
			.getResourceSettings()
			.getLocalizer()
			.getString(resourceKey, null,
				"Server parsetime: ${servertime}, Client parsetime: ${clienttime}");
		final Map<String, String> map = new HashMap<String, String>(4);
		map.put("clienttime", "' + (new Date().getTime() - clientTimeVariable)/1000 +  's';");
		map.put("servertime", ((double)timeTaken) / 1000 + "s");
		return MapVariableInterpolator.interpolate(txt, map);
	}

	/**
	 * Get CSP nonce
	 */
	protected String getNonce() {
		return null;
	}

}