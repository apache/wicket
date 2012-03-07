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
			AppendingStringBuffer endScript = new AppendingStringBuffer(150);
			endScript.append("\n").append(JavaScriptUtils.SCRIPT_OPEN_TAG);
			endScript.append("\nwindow.defaultStatus='");
			endScript.append(getStatusString(timeTaken, "ServerAndClientTimeFilter.statustext"));
			endScript.append("';\n").append(JavaScriptUtils.SCRIPT_CLOSE_TAG).append("\n");
			responseBuffer.insert(bodyIndex - 1, endScript);
			responseBuffer.insert(headIndex + 6, "\n" + JavaScriptUtils.SCRIPT_OPEN_TAG +
				"\nvar clientTimeVariable = new Date().getTime();\n" +
				JavaScriptUtils.SCRIPT_CLOSE_TAG + "\n");
		}
		else if (ajaxStart != -1 && ajaxEnd != -1)
		{
			AppendingStringBuffer startScript = new AppendingStringBuffer(250);
			startScript.append("<evaluate><![CDATA[window.defaultStatus='");
			startScript.append(getStatusString(timeTaken,
				"ajax.ServerAndClientTimeFilter.statustext"));
			startScript.append("';]]></evaluate>");
			responseBuffer.insert(ajaxEnd, startScript.toString());
			responseBuffer.insert(ajaxStart + 15,
				"<priority-evaluate><![CDATA[clientTimeVariable = new Date().getTime();]]></priority-evaluate>");
		}
		log.info(timeTaken + "ms server time taken for request " +
			RequestCycle.get().getRequest().getUrl() + " response size: " + responseBuffer.length());
		return responseBuffer;
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
		map.put("clienttime", "' + (new Date().getTime() - clientTimeVariable)/1000 +  's");
		map.put("servertime", ((double)timeTaken) / 1000 + "s");
		return MapVariableInterpolator.interpolate(txt, map);
	}
}