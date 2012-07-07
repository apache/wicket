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
package org.apache.wicket.examples;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.response.filter.IResponseFilter;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;


/**
 * Displays server host name (combination of name, ipaddress and unique id, which is either based)
 * and time it took to handle the request in the browser's status bar like this:
 * <code>window.defaultStatus = 'Host: myhost/192.168.1.66/someid, handled in: 0.01s'</code>
 * 
 * @author eelco hillenius
 */
public class ServerHostNameAndTimeFilter implements IResponseFilter
{
	private String host;

	/**
	 * Construct, trying system property 'examples.hostname' for the server id or else current time
	 * milis.
	 */
	public ServerHostNameAndTimeFilter()
	{
		String hostId = null;
		try
		{
			hostId = System.getProperty("examples.hostname");
		}
		catch (SecurityException ignored)
		{
		}
		if (Strings.isEmpty(hostId))
		{
			hostId = String.valueOf(System.currentTimeMillis());
		}

		setHostName(hostId);
	}

	/**
	 * Construct with an id.
	 * 
	 * @param hostId
	 *            a unique id indentifying this server instance
	 */
	public ServerHostNameAndTimeFilter(String hostId)
	{
		if (hostId == null)
		{
			throw new IllegalArgumentException("hostId may not be null");
		}

		setHostName(hostId);
	}

	/**
	 * @see IResponseFilter#filter(AppendingStringBuffer)
	 */
	@Override
	public AppendingStringBuffer filter(AppendingStringBuffer responseBuffer)
	{
		int index = responseBuffer.indexOf("<head>");
		long timeTaken = System.currentTimeMillis() - RequestCycle.get().getStartTime();

		if (index != -1)
		{
			AppendingStringBuffer script = new AppendingStringBuffer(75);
			script.append("\n");
			script.append(JavaScriptUtils.SCRIPT_OPEN_TAG);
			script.append("\n\twindow.defaultStatus='");
			script.append("Host: ");
			script.append(host);
			script.append(", handled in: ");
			script.append(Duration.milliseconds(timeTaken));
			script.append("';\n");
			script.append(JavaScriptUtils.SCRIPT_CLOSE_TAG);
			script.append("\n");
			responseBuffer.insert(index + 6, script);
		}
		return responseBuffer;
	}

	/**
	 * Fill host name property.
	 * 
	 * @param hostId
	 */
	private void setHostName(String hostId)
	{
		try
		{
			InetAddress localMachine = InetAddress.getLocalHost();
			String hostName = localMachine.getHostName();
			String address = localMachine.getHostAddress();
			host = ((!Strings.isEmpty(hostName)) ? hostName + "/" + address : address) + "/" +
				hostId;
		}
		catch (UnknownHostException ignored)
		{
		}

		if (Strings.isEmpty(host))
		{
			host = "<unknown>";
		}
	}
}