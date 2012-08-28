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
package org.apache.wicket.core.util.string;

import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Various url utilities
 *
 * @author igor.vaynberg
 * @deprecated Use {@link org.apache.wicket.request.UrlUtils} instead.
 */
@Deprecated
public class UrlUtils extends org.apache.wicket.request.UrlUtils
{
	/**
	 * Rewrites a relative url to be context relative, leaves absolute urls same.
	 *
	 * @param url
	 * @param requestCycle
	 * @return rewritten url
	 * @deprecated use {@link #rewriteToContextRelative(String, org.apache.wicket.request.IRequestCycle)} instead
	 */
	@Deprecated
	public static String rewriteToContextRelative(String url, RequestCycle requestCycle)
	{
		if (isRelative(url))
		{
			return requestCycle.getUrlRenderer().renderContextRelativeUrl(url);
		}
		else
		{
			return url;
		}
	}
}