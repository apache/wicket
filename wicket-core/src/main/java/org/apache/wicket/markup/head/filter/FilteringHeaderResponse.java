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
package org.apache.wicket.markup.head.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.internal.HeaderResponse;
import org.apache.wicket.markup.html.DecoratingHeaderResponse;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.response.StringResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This header response allows you to separate things that are added to the IHeaderResponse into
 * different buckets. Then, you can render those different buckets in separate areas of the page
 * based on your filter logic. A typical use case for this header response is to move the loading of
 * JavaScript files (and inline script tags) to the footer of the page.
 * 
 * @see HeaderResponseContainer
 * @see CssAcceptingHeaderResponseFilter
 * @see JavaScriptAcceptingHeaderResponseFilter
 * @author Jeremy Thomerson
 * @author Emond Papegaaij
 */
public class FilteringHeaderResponse extends DecoratingHeaderResponse
{

	private static final Logger log = LoggerFactory.getLogger(FilteringHeaderResponse.class);

	/**
	 * The default name of the filter that will collect contributions which should be rendered
	 * in the page's &lt;head&gt;
	 */
	public static final String DEFAULT_HEADER_FILTER_NAME = "wicket-default-header-filter";

	/**
	 * A filter used to bucket your resources, inline scripts, etc, into different responses. The
	 * bucketed resources are then rendered by a {@link HeaderResponseContainer}, using the name of
	 * the filter to get the correct bucket.
	 * 
	 * @author Jeremy Thomerson
	 */
	public interface IHeaderResponseFilter extends Predicate<HeaderItem>
	{
		/**
		 * @return name of the filter (used by the container that renders these resources)
		 */
		String getName();

		/**
		 * Determines whether a given HeaderItem should be rendered in the bucket represented by
		 * this filter.
		 * 
		 * @param item
		 *            the item to be rendered
		 * @return true if it should be bucketed with other things in this filter
		 */
		boolean accepts(HeaderItem item);

		@Override
		default boolean test(HeaderItem item) {
			return accepts(item);
		}
	}

	/**
	 * we store this FilteringHeaderResponse in the RequestCycle so that the containers can access
	 * it to render their bucket of stuff
	 */
	private static final MetaDataKey<FilteringHeaderResponse> RESPONSE_KEY = new MetaDataKey<FilteringHeaderResponse>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final Map<String, List<HeaderItem>> responseFilterMap = new HashMap<String, List<HeaderItem>>();
	private Iterable<? extends IHeaderResponseFilter> filters;
	private final String headerFilterName;

	/**
	 * Constructor without explicit filters.
	 *
	 * Generates filters automatically for any FilteredHeaderItem.
	 * Any other contribution is rendered in the page's &lt;head&gt;
	 *
	 * @param response
	 *            the wrapped IHeaderResponse
	 * @see HeaderResponseContainer
	 */
	public FilteringHeaderResponse(IHeaderResponse response)
	{
		this(response, DEFAULT_HEADER_FILTER_NAME, Collections.<IHeaderResponseFilter>emptyList());
	}

	/**
	 * Construct.
	 * 
	 * @param response
	 *            the wrapped IHeaderResponse
	 * @param headerFilterName
	 *            the name that the filter for things that should appear in the head (default Wicket
	 *            location) uses
	 * @param filters
	 *            the filters to use to bucket things. There will be a bucket created for each
	 *            filter, by name. There should typically be at least one filter with the same name
	 *            as your headerFilterName
	 */
	public FilteringHeaderResponse(IHeaderResponse response, String headerFilterName,
		Iterable<? extends IHeaderResponseFilter> filters)
	{
		super(response);
		this.headerFilterName = headerFilterName;

		setFilters(filters);

		RequestCycle.get().setMetaData(RESPONSE_KEY, this);
	}

	protected void setFilters(Iterable<? extends IHeaderResponseFilter> filters)
	{
		this.filters = filters;
		if (filters == null)
		{
			return;
		}
		for (IHeaderResponseFilter filter : filters)
		{
			responseFilterMap.put(filter.getName(), new ArrayList<HeaderItem>());
		}
	}

	/**
	 * @return the FilteringHeaderResponse being used in this RequestCycle
	 */
	public static FilteringHeaderResponse get()
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle == null)
		{
			throw new IllegalStateException(
				"You can only get the FilteringHeaderResponse when there is a RequestCycle present");
		}
		FilteringHeaderResponse response = requestCycle.getMetaData(RESPONSE_KEY);
		if (response == null)
		{
			throw new IllegalStateException(
				"No FilteringHeaderResponse is present in the request cycle.  This may mean that you have not decorated the header response with a FilteringHeaderResponse.  Simply calling the FilteringHeaderResponse constructor sets itself on the request cycle");
		}
		return response;
	}

	@Override
	public void render(HeaderItem item)
	{
		if (item instanceof FilteredHeaderItem)
		{
			String filterName = ((FilteredHeaderItem)item).getFilterName();

			if (responseFilterMap.containsKey(filterName) == false)
			{
				responseFilterMap.put(filterName, new ArrayList<HeaderItem>());
			}

			render(item, filterName);
		}
		else
		{
			if (filters != null)
			{
				for (IHeaderResponseFilter filter : filters)
				{
					if (filter.accepts(item))
					{
						render(item, filter.getName());
						return;
					}
				}
			}

			// none of the configured filters accepted it so put it in the header
			if (responseFilterMap.containsKey(headerFilterName) == false)
			{
				responseFilterMap.put(headerFilterName, new ArrayList<HeaderItem>());
			}
			render(item, headerFilterName);
			log.debug("A HeaderItem '{}' was rendered to the filtering header response, but did not match any filters, so it put in the <head>.",
					item);
		}
	}

	@Override
	public void close()
	{
		// write the stuff that was actually supposed to be in the header to the
		// response, which is used by the built-in HtmlHeaderContainer to get
		// its contents

		CharSequence headerContent = getContent(headerFilterName);
		RequestCycle.get().getResponse().write(headerContent);
		// must make sure our super (and with it, the wrapped response) get closed:
		super.close();
	}

	/**
	 * Gets the content that was rendered to this header response and matched the filter with the
	 * given name.
	 * 
	 * @param filterName
	 *            the name of the filter to get the bucket for
	 * @return the content that was accepted by the filter with this name
	 */
	@SuppressWarnings("resource")
	public final CharSequence getContent(String filterName)
	{
		if (filterName == null || !responseFilterMap.containsKey(filterName))
		{
			return "";
		}
		List<HeaderItem> resp = responseFilterMap.get(filterName);
		final StringResponse strResponse = new StringResponse();
		IHeaderResponse headerRenderer = new HeaderResponse()
		{
			@Override
			protected Response getRealResponse()
			{
				return strResponse;
			}

			@Override
			public boolean wasRendered(Object object)
			{
				return FilteringHeaderResponse.this.getRealResponse().wasRendered(object);
			}

			@Override
			public void markRendered(Object object)
			{
				FilteringHeaderResponse.this.getRealResponse().markRendered(object);
			}
		};

		headerRenderer = decorate(headerRenderer);

		for (HeaderItem curItem : resp)
		{
			headerRenderer.render(curItem);
		}

		headerRenderer.close();

		return strResponse.getBuffer();
	}

	/**
	 * Decorate the given response used to get contents.
	 * 
	 * @param response
	 *            response to decorate
	 * @return default implementation just returns the response
	 */
	protected IHeaderResponse decorate(IHeaderResponse response)
	{
		return response;
	}

	private void render(HeaderItem item, String filterName)
	{
		if (responseFilterMap.containsKey(filterName) == false)
		{
			throw new IllegalArgumentException("No filter named '" + filterName +
				"', known filter names are: " + responseFilterMap.keySet());
		}
		render(item, responseFilterMap.get(filterName));
	}

	protected void render(HeaderItem item, List<HeaderItem> filteredItems)
	{
		if (RequestCycle.get().find(IPartialPageRequestHandler.class).isPresent())
		{
			// we're in an ajax request, so we don't filter and separate stuff....
			getRealResponse().render(item);
			return;
		}
		filteredItems.add(item);
	}
}
