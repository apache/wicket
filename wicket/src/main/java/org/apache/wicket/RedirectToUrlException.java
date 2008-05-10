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
package org.apache.wicket;

import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;

/**
 * Causes wicket to interrupt current request processing and send a redirect to the given url.
 * 
 * Use this if you want to redirect to an external or none wicket url. If you want to redirect to a
 * page use the {@link RestartResponseException}
 * 
 * @author jcompagner
 * 
 * @see RestartResponseException
 * @see RestartResponseAtInterceptPageException
 */
public class RedirectToUrlException extends AbstractRestartResponseException
{

	private static final long serialVersionUID = 1L;


	/**
	 * Your URL should be one of the following:
	 * <ul>
	 * <li>Fully qualified "http://foo.com/bar"</li>
	 * <li>Relative to the Wicket filter/servlet, e.g. "?wicket:interface=foo", "mounted_page"</li>
	 * <li>Absolute within your web application's context root, e.g. "/foo.html"</li>
	 * </ul>
	 * 
	 * @param url
	 *            URL to redirect to.
	 */
	public RedirectToUrlException(String url)
	{
		RequestCycle rc = RequestCycle.get();
		if (rc == null)
		{
			throw new IllegalStateException(
				"This exception can only be thrown from within request processing cycle");
		}
		else
		{
			Response r = rc.getResponse();
			if (!(r instanceof WebResponse))
			{
				throw new IllegalStateException(
					"This exception can only be thrown when wicket is processing an http request");
			}


			// abort any further response processing
			rc.setRequestTarget(new RedirectRequestTarget(url));
		}
	}
}
