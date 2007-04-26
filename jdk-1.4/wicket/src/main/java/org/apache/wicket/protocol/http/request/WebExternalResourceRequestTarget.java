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
package org.apache.wicket.protocol.http.request;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.WebExternalResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Request target that is not a Wicket resource. For example, such a resource
 * could denote an image in the web application directory (not mapped to a
 * Wicket servlet). NOTE: this target can only be used in a servlet environment
 * with {@link org.apache.wicket.protocol.http.WebRequestCycle}s.
 * 
 * <p>
 * <b>NOTE:</b> this class is a wrapper around
 * {@link ResourceStreamRequestTarget#ResourceStreamRequestTarget(WebExternalResourceStream)},
 * and kept for compatibility purposes.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class WebExternalResourceRequestTarget extends ResourceStreamRequestTarget
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(WebExternalResourceRequestTarget.class);

	/** the relative url of the external resource. */
	private final String uri;

	/**
	 * Construct.
	 * 
	 * @param uri
	 *            the relative url of the external resource
	 */
	public WebExternalResourceRequestTarget(String uri)
	{
		super(new WebExternalResourceStream(uri));
		this.uri = uri;
	}

	/**
	 * Gets the url to the external resource.
	 * 
	 * @return the url to the external resource
	 */
	public final String getUrl()
	{
		return uri;
	}

	/**
	 * @see org.apache.wicket.IRequestTarget#detach(org.apache.wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof WebExternalResourceRequestTarget)
		{
			WebExternalResourceRequestTarget that = (WebExternalResourceRequestTarget)obj;
			return uri.equals(that.uri);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = "WebExternalResourceRequestTarget".hashCode();
		result += uri.hashCode();
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[WebExternalResourceRequestTarget@" + hashCode() + " " + uri + "]";
	}
}
