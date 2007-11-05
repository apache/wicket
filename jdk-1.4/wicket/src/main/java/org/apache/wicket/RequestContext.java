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

import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * @author Ate Douma
 */
public class RequestContext
{
	/** Thread-local that holds the current request context. */
	private static final ThreadLocal current = new ThreadLocal();

	/**
	 * Construct.
	 */
	public RequestContext()
	{
		set(this);
	}

	/**
	 * @return The current threads request context, will make one if there wasn't one.
	 */
	public static final RequestContext get()
	{
		RequestContext context = (RequestContext)current.get();
		if (context == null)
		{
			context = new RequestContext();
		}
		return context;
	}

	/**
	 * 
	 */
	public static final void unset()
	{
		current.set(null);
	}

	protected static final void set(RequestContext context)
	{
		current.set(context);
	}

	/**
	 * @return CharSequence
	 */
	public CharSequence getNamespace()
	{
		return "";
	}

	/**
	 * @param markupId
	 * @return The encoded markup
	 */
	public String encodeMarkupId(String markupId)
	{
		return markupId;
	}

	/**
	 * @param path
	 * @return The encoded url
	 */
	public CharSequence encodeActionURL(CharSequence path)
	{
		return path;
	}

	/**
	 * @param path
	 * @return The encoded url
	 */
	public CharSequence encodeRenderURL(CharSequence path)
	{
		return path;
	}

	/**
	 * @param path
	 * @return The encoded url
	 */
	public CharSequence encodeResourceURL(CharSequence path)
	{
		return path;
	}

	/**
	 * @param path
	 * @return The encoded url
	 */
	public CharSequence encodeSharedResourceURL(CharSequence path)
	{
		return path;
	}

	/**
	 * @return The IHeaderResponse
	 */
	public IHeaderResponse getHeaderResponse()
	{
		return null;
	}

	/**
	 * @return boolean if this is a portlet request
	 */
	public boolean isPortletRequest()
	{
		return false;
	}
}
