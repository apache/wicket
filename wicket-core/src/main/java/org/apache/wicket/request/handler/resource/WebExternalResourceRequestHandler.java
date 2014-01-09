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
package org.apache.wicket.request.handler.resource;

import org.apache.wicket.core.util.resource.WebExternalResourceStream;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.resource.ContentDisposition;


/**
 * Request target that is not a Wicket resource. For example, such a resource could denote an image
 * in the web application directory (not mapped to a Wicket servlet). NOTE: this target can only be
 * used in a servlet environment with {@link org.apache.wicket.request.cycle.RequestCycle}s.
 * 
 * <p>
 * <b>NOTE:</b> this class is a wrapper around
 * {@link ResourceStreamRequestHandler#ResourceStreamRequestHandler(org.apache.wicket.util.resource.IResourceStream)}
 * , and kept for compatibility purposes.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class WebExternalResourceRequestHandler extends ResourceStreamRequestHandler
{
	/** the relative url of the external resource. */
	private final String uri;

	/**
	 * Construct.
	 * 
	 * @param uri
	 *            the relative url of the external resource
	 */
	public WebExternalResourceRequestHandler(String uri)
	{
		super(new WebExternalResourceStream(uri));
		this.uri = uri;
		setContentDisposition(ContentDisposition.INLINE);
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
	 * @see org.apache.wicket.request.IRequestHandler#detach(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void detach(IRequestCycle requestCycle)
	{
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof WebExternalResourceRequestHandler)
		{
			WebExternalResourceRequestHandler that = (WebExternalResourceRequestHandler)obj;
			return uri.equals(that.uri);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = "WebExternalResourceRequestTarget".hashCode();
		result += uri.hashCode();
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[WebExternalResourceRequestTarget@" + hashCode() + " " + uri + "]";
	}
}
