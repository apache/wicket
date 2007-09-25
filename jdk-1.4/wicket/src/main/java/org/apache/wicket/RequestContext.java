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
	
	public RequestContext()
	{
		set(this);
	}
	
	public static final RequestContext get()
	{
		RequestContext context = (RequestContext)current.get();
		if (context == null)
		{
			context = new RequestContext();
		}
		return context;
	}
	
	protected static final void set(RequestContext context)
	{
		current.set(context);
	}

	public CharSequence getNamespace()
	{
		return "";
	}
	
	public String encodeMarkupId(String markupId)
	{
		return markupId;
	}
	
	public CharSequence encodeActionURL(CharSequence path)
	{
		return path;
	}
	
	public CharSequence encodeRenderURL(CharSequence path)
	{
		return path;
	}
	
	public CharSequence encodeResourceURL(CharSequence path)
	{
		return path;
	}
	
	public CharSequence encodeSharedResourceURL(CharSequence path)
	{
		return path;
	}
	
	public IHeaderResponse getHeaderResponse()
	{
		return null;
	}
	
	public boolean isPortletRequest()
	{
		return false;
	}
}
