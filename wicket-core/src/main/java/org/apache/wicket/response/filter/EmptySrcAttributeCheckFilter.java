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

import org.apache.wicket.util.string.AppendingStringBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The EmptySrcAttributeFilter checks if an empty src attribute is found in the response.
 * 
 * Empty src attribute is problematic as it makes some browsers, i.e. firefox, request an URL twice.
 * Two issues arise:
 * 
 * 1. Unnecessary server load.
 * 
 * 2. If the browser only renders the first response, some links in the page might be broken as
 * wicket rerendered them in the second request and dropped the ones rendered in the first request.
 */
public class EmptySrcAttributeCheckFilter implements IResponseFilter
{
	private static final Logger log = LoggerFactory.getLogger(EmptySrcAttributeCheckFilter.class);

	/**
	 * Indicates that an empty src attribute is found in the response.
	 */
	public static final EmptySrcAttributeCheckFilter INSTANCE = new EmptySrcAttributeCheckFilter();

	@Override
	public AppendingStringBuffer filter(final AppendingStringBuffer responseBuffer)
	{
		int pos = responseBuffer.indexOf("src=\"\"");
		if (pos < 0)
		{
			pos = responseBuffer.indexOf("src=''");
			if (pos < 0)
			{
				pos = responseBuffer.indexOf("src=\"#\"");
				if (pos < 0)
				{
					pos = responseBuffer.indexOf("src='#'");
				}
			}
		}
		if (pos >= 0)
		{
			log.warn("Empty src attribute found in response:");
			int from = Math.max(0, pos - 32);
			int to = Math.min(pos + 32, responseBuffer.length());
			log.warn("[...]" + responseBuffer.substring(from, to) + "[...]");
		}
		return responseBuffer;
	}
}
