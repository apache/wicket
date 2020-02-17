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

import org.apache.wicket.Application;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.AppendingStringBuffer;
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
 * @deprecated This class has been deprecated for several reasons. The way it tries to measure
 *             server and client times is very inaccurate. Modern browsers provide much better tools
 *             to measure Javascript execution times. The measurements were written in a property
 *             that has been deprecated for years and removed in modern browsers. Finally, rendering
 *             the Javascript directly into the response makes it hard to support a strict CSP with
 *             nonces. There is no real replacement for this class. Use the tools provided by the
 *             browser. See {@code WicketExampleApplication} for a simple example of passing
 *             rendering times to the browser via the {@code Server-Timing} header.
 */
@Deprecated
public class AjaxServerAndClientTimeFilter implements IResponseFilter
{
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
		if (headIndex != -1 && bodyIndex != -1)
		{
			responseBuffer.insert(bodyIndex, scriptTag("window.defaultStatus=" + getStatusString(timeTaken, "ServerAndClientTimeFilter.statustext") + ";"));
			responseBuffer.insert(headIndex + 6, scriptTag("clientTimeVariable = new Date().getTime();"));
		}
		else if (ajaxStart != -1 && ajaxEnd != -1)
		{
			responseBuffer.insert(ajaxEnd, headerContribution("window.defaultStatus=" + getStatusString(timeTaken, "ajax.ServerAndClientTimeFilter.statustext") + ";"));
			responseBuffer.insert(ajaxStart + 15, headerContribution("clientTimeVariable = new Date().getTime();"));
		}
		log.info(timeTaken + "ms server time taken for request " +
			RequestCycle.get().getRequest().getUrl() + " response size: " + responseBuffer.length());
		return responseBuffer;
	}

	private String scriptTag(String script)
	{
		AppendingStringBuffer buffer = new AppendingStringBuffer(250);
		buffer.append("\n");
		buffer.append(JavaScriptUtils.SCRIPT_OPEN_TAG);
		buffer.append(script);
		buffer.append(JavaScriptUtils.SCRIPT_CLOSE_TAG).append("\n");
		return buffer.toString();
	}
	
	private String headerContribution(String script)
	{
		AppendingStringBuffer buffer = new AppendingStringBuffer(250);
		buffer.append("<header-contribution><![CDATA[<head xmlns:wicket=\"http://wicket.apache.org\">");
		buffer.append(script);
		buffer.append("</head>]]></header-contribution>");
		return buffer.toString();
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
		final HashMap<String, String> map = new HashMap<String, String>(4);
		map.put("servertime", ((double)timeTaken) / 1000 + "s");
		map.put("clienttime", "' + (new Date().getTime() - clientTimeVariable)/1000 +  's");

		return Application.get()
			.getResourceSettings()
			.getLocalizer()
			.getString(resourceKey, null, Model.of(map),
				"'Server parsetime: ${servertime}, Client parsetime: ${clienttime}'");
	}
}