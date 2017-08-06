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
package org.apache.wicket.http2.markup.head;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * The object to be pushed. See the urlFor methods of {@link Component} to know what can be used in
 * addition to {@link String}.
 * 
 * @author Tobias Soloschenko
 */
public class PushItem implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Serializable object;

	private PageParameters pageParameters;

	private String url;

	private Map<String, PushItemHeaderValue> headers = new HashMap<>();

	/**
	 * Creates a push item
	 * 
	 * @param object
	 *            the object to extract the push URL information from
	 * @param pageParameters
	 *            the page parameters
	 * @param headers
	 *            the headers to be applied to the push
	 */
	public PushItem(Serializable object, PageParameters pageParameters,
		Map<String, PushItemHeaderValue> headers)
	{
		this.object = object;
		this.pageParameters = pageParameters;
		if (headers != null)
		{
			this.headers = headers;
		}
	}

	/**
	 * Creates a push item
	 * 
	 * @param object
	 *            the object to extract the push URL information from
	 * @param headers
	 *            the headers to be applied to the push
	 */
	public PushItem(Serializable object, Map<String, PushItemHeaderValue> headers)
	{
		this(object, null, headers);
	}

	/**
	 * Creates a push item
	 * 
	 * @param object
	 *            the object to extract the push URL information from
	 * @param pageParameters
	 *            the page parameters
	 */
	public PushItem(Serializable object, PageParameters pageParameters)
	{
		this(object, pageParameters, null);
	}

	/**
	 * Creates a push item
	 * 
	 * @param object
	 *            the object to extract the push URL information from
	 */
	public PushItem(Serializable object)
	{
		this(object, null, null);
	}

	/**
	 * Creates a push item
	 */
	public PushItem()
	{
	}

	/**
	 * Gets the object which contains the push URL information
	 * 
	 * @return the object to extract the push URL information from
	 */
	public Object getObject()
	{
		return object;
	}

	/**
	 * Sets the object which contains the push URL information
	 * 
	 * @param object
	 *            the object to extract the push URL information from
	 * @see {@link org.apache.wicket.request.cycle.RequestCycle} (urlFor methods)
	 * @return the push item
	 */
	public PushItem setObject(Serializable object)
	{
		this.object = object;
		return this;
	}

	/**
	 * Gets the page parameters
	 * 
	 * @return the page parameters
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * Sets the page parameters
	 * 
	 * @param pageParameters
	 *            the page parameters
	 * @return the push item
	 */
	public PushItem setPageParameters(PageParameters pageParameters)
	{
		this.pageParameters = pageParameters;
		return this;
	}

	/**
	 * Gets the URL composed within the push header item
	 * 
	 * @see {@link org.apache.wicket.http2.markup.head.PushHeaderItem#push(List)}
	 * @return the URL to be pushed
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * Sets the URL composed within the push header item
	 * 
	 * @see {@link org.apache.wicket.http2.markup.head.PushHeaderItem#push(List)}
	 * @param url
	 *            the URL used to push the resource
	 * @return the push item
	 */
	public PushItem setUrl(String url)
	{
		this.url = url;
		return this;
	}

	/**
	 * Gets the headers to be added to the push response
	 * 
	 * @return the headers to be added to the push response
	 */
	public Map<String, PushItemHeaderValue> getHeaders()
	{
		return headers;
	}

	/**
	 * Sets the headers to be added to the push response
	 * 
	 * @param headers
	 *            the headers to be added to the push response
	 * @return the push item
	 */
	public PushItem setHeaders(Map<String, PushItemHeaderValue> headers)
	{
		this.headers = headers;
		return this;
	}
}
