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
package org.apache.wicket.request.target.coding;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.UnitTestSettings;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.value.ValueMap;

/**
 * Encodes and decodes mounts for a single bookmarkable page class, but with the parameters appended
 * in a URL query string rather than integrated into a URL hierarchical path.
 * <p>
 * For example, whereas
 * {@link org.apache.wicket.request.target.coding.BookmarkablePageRequestTargetUrlCodingStrategy BookmarkablePageRequestTargetUrlCodingStrategy}
 * might encode a request target as
 * "mywebapp/myservlet/admin/productmanagement/action/edit/product/4995",
 * <code>QueryStringRequestTargetUrlCodingStrategy</code> would encode the same target as
 * "mywebapp/myservlet/admin/productmanagement?action=edit&amp;product=4995".
 * <p>
 * URLs encoded in this way can be bookmarked just as easily as those produced by
 * <code>BookmarkablePageRequestTargetUrlCodingStrategy</code>. For example, Google searches
 * produce bookmarkable links with query strings.
 * <p>
 * Whether <code>BookmarkablePageRequestTargetUrlCodingStrategy</code> or
 * <code>QueryStringRequestTargetUrlCodingStrategy</code> is appropriate for a given mount depends
 * on:
 * <ul>
 * <li>Esthetic criteria
 * <li>Interpretations of <a href="http://www.gbiv.com/protocols/uri/rfc/rfc3986.html">RFC 3986</a>.
 * This defines the URI standard, including query strings, and states that whereas the "path
 * component contains data, usually organized in hierarchical form [divided by slashes]", the "query
 * component [after the question mark] contains non-hierarchical data".
 * <li>Findability. Public search engines prefer URLs with parameters stored hierarchically or in a
 * shorter query string. Google's <a
 * href="http://www.google.com/support/webmasters/bin/answer.py?answer=35770">Design and Content
 * Guidelines</a> (as of May 6 2006) state: "Make a site with a clear hierarchy and text links.
 * Every page should be reachable from at least one static text link. &#8230; If you decide to use
 * dynamic pages (i.e., the URL contains a '?' character), be aware that not every search engine
 * spider crawls dynamic pages as well as static pages. It helps to keep the parameters short and
 * the number of them few."
 * <li>The complexity of the parameters being passed. More complex parameters may make more sense
 * expressed as a series of "key=value(s)" pairs in a query string than shoehorned into a
 * hierarchical structure.
 * </ul>
 * <p>
 * Regardless of which coding strategy is chosen for the mount,
 * {@link org.apache.wicket.markup.html.link.BookmarkablePageLink BookmarkablePageLink} can be used
 * to insert a bookmarkable link to the request target.
 * <p>
 * This example demonstrates how to mount a path with
 * <code>QueryStringRequestTargetUrlCodingStrategy</code> within the <code>init</code> method of
 * a class implementing {@link org.apache.wicket.protocol.http.WebApplication WebApplication}:
 * <p>
 * <code>mount(new QueryStringUrlCodingStrategy("/admin/productmanagement", admin.ProductManagement.class));</code>
 * <p>
 * Note that, as with the main BookmarkablePageRequestTargetUrlCodingStrategy, if the output of this
 * coding strategy is passed through
 * {@link javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String) HttpServletResponse.encodeURL}
 * and the client has cookies turned off, the client's session ID will be stored in a path
 * parameter, like so:
 * "/mywebapp/myservlet/admin/productmanagement;jsessionid=730EC527564AF1C73F8C2FB19B604F55?action=edit&amp;product=4995".
 * 
 * @author Benjamin Hawkes-Lewis
 */
public class QueryStringUrlCodingStrategy extends BookmarkablePageRequestTargetUrlCodingStrategy
{

	/**
	 * Sole constructor.
	 * 
	 * @param mountPath
	 *            the relative reference URL on which the page is mounted
	 * @param bookmarkablePageClass
	 *            the class of the mounted page
	 */
	public QueryStringUrlCodingStrategy(final String mountPath, final Class bookmarkablePageClass)
	{
		super(mountPath, bookmarkablePageClass, PageMap.DEFAULT_NAME);
	}

	/**
	 * Append the parameters to the end of the URL.
	 * 
	 * @param url
	 *            the relative reference URL
	 * @param parameters
	 *            parameter names mapped to parameter values
	 */
	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{
		if (!url.endsWith("/"))
		{
			url.append("/");
		}
		if (parameters != null && parameters.size() > 0)
		{
			final Iterator entries;
			if (UnitTestSettings.getSortUrlParameters())
			{
				entries = new TreeMap(parameters).entrySet().iterator();
			}
			else
			{
				entries = parameters.entrySet().iterator();
			}
			WebRequestEncoder encoder = new WebRequestEncoder(url);
			while (entries.hasNext())
			{
				Map.Entry entry = (Entry)entries.next();

				if (entry.getValue() != null)
				{
					encoder.addValue(entry.getKey().toString(), entry.getValue());
				}
			}
		}
	}

	public IRequestTarget decode(RequestParameters requestParameters)
	{
		String pageMapName = requestParameters.getPageMapName();
		final PageParameters parameters = new PageParameters(requestParameters.getParameters());

		// This might be a request to a stateless page, so check for an
		// interface name.
		if (requestParameters.getInterfaceName() != null)
		{
			return new BookmarkableListenerInterfaceRequestTarget(pageMapName,
					(Class)bookmarkablePageClassRef.get(), parameters, requestParameters
							.getComponentPath(), requestParameters.getInterfaceName(),
					requestParameters.getVersionNumber());
		}
		else
		{
			return new BookmarkablePageRequestTarget(pageMapName, (Class)bookmarkablePageClassRef
					.get(), parameters);
		}
	}

	/**
	 * Decodes parameters object from the provided query string
	 * 
	 * @param fragment
	 *            contains the query string
	 * @param passedParameters
	 *            parameters decoded by wicket before this method - usually off the query string
	 * 
	 * @return Parameters
	 */
	protected ValueMap decodeParameters(String fragment, Map passedParameters)
	{
		ValueMap parameters = new ValueMap();

		if (passedParameters != null)
		{
			parameters.putAll(passedParameters);
		}

		return parameters;

	}

}
