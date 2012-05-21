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
package org.apache.wicket.markup.head.internal;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.response.NullResponse;


/**
 * Default implementation of the {@link org.apache.wicket.markup.head.IHeaderResponse} interface.
 * 
 * @author Matej Knopp
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class HeaderResponse implements IHeaderResponse
{
	/**
	 * A key used to keep the set of rendered resources in the request cycle's meta data
	 */
	private static final MetaDataKey<Set<Object>> RENDERED_RESOURCES = new MetaDataKey<Set<Object>>()
	{
	};

	private boolean closed;

	/**
	 * @see org.apache.wicket.markup.head.IHeaderResponse#markRendered(java.lang.Object)
	 */
	@Override
	public final void markRendered(Object object)
	{
		Set<Object> rendered = getRenderedResources();
		rendered.add(object);
	}

	@Override
	public void render(HeaderItem item)
	{
		if (!closed && !wasItemRendered(item))
		{
			item.render(getResponse());
			markItemRendered(item);
		}
	}

	protected boolean wasItemRendered(HeaderItem item)
	{
		for (Object curToken : item.getRenderTokens())
		{
			if (wasRendered(curToken))
				return true;
		}
		return false;
	}

	protected void markItemRendered(HeaderItem item)
	{
		for (Object curToken : item.getRenderTokens())
		{
			markRendered(curToken);
		}
	}

	/**
	 * @see org.apache.wicket.markup.head.IHeaderResponse#wasRendered(java.lang.Object)
	 */
	@Override
	public final boolean wasRendered(Object object)
	{
		Set<Object> rendered = getRenderedResources();
		return rendered.contains(object);
	}

	/**
	 * @see org.apache.wicket.markup.head.IHeaderResponse#close()
	 */
	@Override
	public void close()
	{
		closed = true;
	}

	/**
	 * @see org.apache.wicket.markup.head.IHeaderResponse#getResponse()
	 */
	@Override
	public final Response getResponse()
	{
		return closed ? NullResponse.getInstance() : getRealResponse();
	}

	/**
	 * @see org.apache.wicket.markup.head.IHeaderResponse#isClosed()
	 */
	@Override
	public boolean isClosed()
	{
		return closed;
	}

	/**
	 * Once the HeaderResponse is closed, no output may be written to it anymore. To enforce that,
	 * the {@link #getResponse()} is defined final in this class and will return a NullResponse
	 * instance once closed or otherwise the Response provided by this method.
	 * 
	 * @return Response
	 */
	protected abstract Response getRealResponse();

	/**
	 * @return the set of rendered resources per request cycle
	 */
	private Set<Object> getRenderedResources()
	{
		RequestCycle requestCycle = RequestCycle.get();
		Set<Object> rendered = requestCycle.getMetaData(RENDERED_RESOURCES);
		if (rendered == null)
		{
			rendered = new HashSet<Object>();
			requestCycle.setMetaData(RENDERED_RESOURCES, rendered);
		}
		return rendered;
	}
}
