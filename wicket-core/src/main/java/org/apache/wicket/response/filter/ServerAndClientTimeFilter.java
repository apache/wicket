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
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a filter that injects javascript code to the top head portion and after the body so that
 * the time can me measured what the client parse time was for this page. It also reports the total
 * server parse/response time in the client and logs the server response time and response size it
 * took for a specific response in the server log.
 * 
 * You can specify what the status text should be like this: ServerAndClientTimeFilter.statustext=My
 * Application, Server parsetime: ${servertime}, Client parsetime: ${clienttime}
 * 
 * <p>
 * Usage: in YourApplication.java:
 * 
 * <pre>
 * &#064;Override
 * public init()
 * {
 * 	super.init();
 * 	getRequestCycleSettings().addResponseFilter(new ServerAndClientTimeFilter());
 * }
 * </pre>
 * 
 * @author jcompagner
 */
public class ServerAndClientTimeFilter implements IResponseFilter
{
	private static final Logger log = LoggerFactory.getLogger(ServerAndClientTimeFilter.class);

	/**
	 * @see IResponseFilter#filter(AppendingStringBuffer)
	 */
	@Override
	public AppendingStringBuffer filter(AppendingStringBuffer responseBuffer)
	{
		int headIndex = responseBuffer.indexOf("<head>");
		int bodyIndex = responseBuffer.indexOf("</body>");
		long timeTaken = System.currentTimeMillis() - RequestCycle.get().getStartTime();
		if (headIndex != -1 && bodyIndex != -1)
		{
			Map<String, String> map = new HashMap<String, String>(4);
			map.put("clienttime", "' + (new Date().getTime() - clientTimeVariable)/1000 +  's");
			map.put("servertime", ((double)timeTaken) / 1000 + "s");

			AppendingStringBuffer defaultValue = new AppendingStringBuffer(128);
			defaultValue.append("Server parsetime: ");
			defaultValue.append(((double)timeTaken) / 1000);
			defaultValue.append("s, Client parsetime: ' + (new Date().getTime() - clientTimeVariable)/1000 +  's");

			String txt = Application.get()
				.getResourceSettings()
				.getLocalizer()
				.getString("ServerAndClientTimeFilter.statustext", null, Model.ofMap(map),
					defaultValue.toString());
			AppendingStringBuffer endScript = new AppendingStringBuffer(150);
			endScript.append("\n").append(JavaScriptUtils.SCRIPT_OPEN_TAG);
			endScript.append("\nwindow.defaultStatus='");
			endScript.append(txt);
			endScript.append("';\n").append(JavaScriptUtils.SCRIPT_CLOSE_TAG).append("\n");
			responseBuffer.insert(bodyIndex - 1, endScript);
			responseBuffer.insert(headIndex + 6, "\n" + JavaScriptUtils.SCRIPT_OPEN_TAG +
				"\nvar clientTimeVariable = new Date().getTime();\n" +
				JavaScriptUtils.SCRIPT_CLOSE_TAG + "\n");
		}
		log.info(timeTaken + "ms server time taken for request " +
			RequestCycle.get().getRequest().getUrl() + " response size: " + responseBuffer.length());
		return responseBuffer;
	}
}