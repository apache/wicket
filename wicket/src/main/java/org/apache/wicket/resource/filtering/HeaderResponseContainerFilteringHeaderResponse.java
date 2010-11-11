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
package org.apache.wicket.resource.filtering;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.DecoratingHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.response.StringResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This header response allows you to separate things that are added to the IHeaderResponse into
 * different buckets. Then, you can render those different buckets in separate areas of the page
 * based on your filter logic. A typical use case for this header response is to move the loading of
 * Javascript files (and inline script tags) to the footer of the page.
 * 
 * @see HeaderResponseFilteredResponseContainer
 * @see CssAcceptingHeaderResponseFilter
 * @see JavascriptAcceptingHeaderResponseFilter
 * @author Jeremy Thomerson
 */
public class HeaderResponseContainerFilteringHeaderResponse extends DecoratingHeaderResponse
{

	private static final Logger log = LoggerFactory.getLogger(Component.class);

	/**
	 * A filter used to bucket your resources, inline scripts, etc, into different responses. The
	 * bucketed resources are then rendered by a {@link HeaderResponseFilteredResponseContainer},
	 * using the name of the filter to get the correct bucket.
	 * 
	 * @author Jeremy Thomerson
	 */
	public static interface IHeaderResponseFilter
	{
		/**
		 * @return name of the filter (used by the container that renders these resources)
		 */
		String getName();

		/**
		 * Determines whether a given ResourceReference should be rendered in the bucket represented
		 * by this filter.
		 * 
		 * @param ref
		 *            the reference to be rendered
		 * @return true if it should be bucketed with other things in this filter
		 */
		boolean acceptReference(ResourceReference ref);

		/**
		 * Whenever a render*Javascript method on IHeaderResponse is called that is not a
		 * ResourceReference (i.e. {@link IHeaderResponse#renderOnDomReadyJavascript(String)}), this
		 * method determines if the script should be bucketed with other things in this filter.
		 * 
		 * Note that calls to IHeaderResponse.renderJavascriptReference(String url) are also
		 * filtered with this method since there is no actual ResourceReference to pass
		 * 
		 * @return true if javascript should be bucketed with other things in this filter
		 */
		boolean acceptOtherJavascript();

		/**
		 * Whenever a renderCSS* method on IHeaderResponse is called that is not a ResourceReference
		 * (i.e. {@link IHeaderResponse#renderCSSReference(String)}, or
		 * {@link IHeaderResponse#renderCSSReference(String, String)}), this method determines if
		 * the CSS reference should be bucketed with other things in this filter.
		 * 
		 * @return true if css should be bucketed with other things in this filter
		 */
		boolean acceptOtherCss();
	}

	/**
	 * we store this HeaderResponseContainerFilteringHeaderResponse in the RequestCycle so that the
	 * containers can access it to render their bucket of stuff
	 */
	private static final MetaDataKey<HeaderResponseContainerFilteringHeaderResponse> RESPONSE_KEY = new MetaDataKey<HeaderResponseContainerFilteringHeaderResponse>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final Map<String, StringResponse> responseFilterMap = new HashMap<String, StringResponse>();
	private IHeaderResponseFilter[] filters;
	private final String headerFilterName;

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
	public HeaderResponseContainerFilteringHeaderResponse(IHeaderResponse response,
		String headerFilterName, IHeaderResponseFilter[] filters)
	{
		super(response);
		this.headerFilterName = headerFilterName;

		setFilters(filters);

		RequestCycle.get().setMetaData(RESPONSE_KEY, this);
	}

	protected void setFilters(IHeaderResponseFilter[] filters)
	{
		this.filters = filters;
		if (filters == null)
		{
			return;
		}
		for (IHeaderResponseFilter filter : filters)
		{
			responseFilterMap.put(filter.getName(), new StringResponse());
		}
	}

	/**
	 * @return the HeaderResponseContainerFilteringHeaderResponse being used in this RequestCycle
	 */
	public static HeaderResponseContainerFilteringHeaderResponse get()
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle == null)
		{
			throw new IllegalStateException(
				"you can only get the HeaderResponseContainerFilteringHeaderResponse when there is a RequestCycle present");
		}
		HeaderResponseContainerFilteringHeaderResponse response = requestCycle.getMetaData(RESPONSE_KEY);
		if (response == null)
		{
			throw new IllegalStateException(
				"no HeaderResponseContainerFilteringHeaderResponse is present in the request cycle.  This may mean that you have not decorated the header response with a HeaderResponseContainerFilteringHeaderResponse.  Simply calling the HeaderResponseContainerFilteringHeaderResponse constructor sets itself on the request cycle");
		}
		return response;
	}

	@Override
	public void renderJavascriptReference(final ResourceReference reference)
	{
		forReference(reference, new Runnable()
		{
			public void run()
			{
				getRealResponse().renderJavascriptReference(reference);
			}
		});
	}

	@Override
	public void renderJavascriptReference(final ResourceReference reference, final String id)
	{
		forReference(reference, new Runnable()
		{
			public void run()
			{
				getRealResponse().renderJavascriptReference(reference, id);
			}
		});
	}

	@Override
	public void renderJavascriptReference(final String url)
	{
		forJavascript(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderJavascriptReference(url);
			}
		});
	}

	@Override
	public void renderJavascriptReference(final String url, final String id)
	{
		forJavascript(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderJavascriptReference(url, id);
			}
		});
	}

	@Override
	public void renderJavascript(final CharSequence javascript, final String id)
	{
		forJavascript(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderJavascript(javascript, id);
			}
		});
	}

	@Override
	public void renderCSSReference(final ResourceReference reference)
	{
		forReference(reference, new Runnable()
		{
			public void run()
			{
				getRealResponse().renderCSSReference(reference);
			}
		});
	}

	@Override
	public void renderCSSReference(final String url)
	{
		forCss(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderCSSReference(url);
			}
		});
	}

	@Override
	public void renderCSSReference(final ResourceReference reference, final String media)
	{
		forReference(reference, new Runnable()
		{
			public void run()
			{
				getRealResponse().renderCSSReference(reference, media);
			}
		});
	}

	@Override
	public void renderCSSReference(final String url, final String media)
	{
		forCss(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderCSSReference(url, media);
			}
		});
	}

	@Override
	public void renderOnDomReadyJavascript(final String javascript)
	{
		forJavascript(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderOnDomReadyJavascript(javascript);
			}
		});
	}

	@Override
	public void renderOnLoadJavascript(final String javascript)
	{
		forJavascript(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderOnLoadJavascript(javascript);
			}
		});
	}

	@Override
	public void renderOnEventJavascript(final String target, final String event,
		final String javascript)
	{
		forJavascript(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderOnEventJavascript(target, event, javascript);
			}
		});
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
	public final CharSequence getContent(String filterName)
	{
		if (filterName == null)
		{
			return "";
		}
		StringResponse resp = responseFilterMap.get(filterName);
		return resp == null ? "" : resp.getBuffer();
	}

	private void forReference(ResourceReference reference, Runnable runnable)
	{
		for (IHeaderResponseFilter filter : filters)
		{
			if (filter.acceptReference(reference))
			{
				run(runnable, filter);
				return;
			}
		}
		log.warn("a ResourceReference was rendered to the filtering header response, but did not match any filters, so it was effectively lost.  Make sure that you have filters that accept every possible case or else configure a default filter that returns true to all acceptance tests");
	}

	private void forJavascript(Runnable runnable)
	{
		for (IHeaderResponseFilter filter : filters)
		{
			if (filter.acceptOtherJavascript())
			{
				run(runnable, filter);
				return;
			}
		}
		log.warn("javascript was rendered to the filtering header response, but did not match any filters, so it was effectively lost.  Make sure that you have filters that accept every possible case or else configure a default filter that returns true to all acceptance tests");
	}

	private void forCss(Runnable runnable)
	{
		for (IHeaderResponseFilter filter : filters)
		{
			if (filter.acceptOtherCss())
			{
				run(runnable, filter);
				return;
			}
		}
		log.warn("css was rendered to the filtering header response, but did not match any filters, so it was effectively lost.  Make sure that you have filters that accept every possible case or else configure a default filter that returns true to all acceptance tests");
	}

	private void run(Runnable runnable, IHeaderResponseFilter filter)
	{
		if (AjaxRequestTarget.get() != null)
		{
			// we're in an ajax request, so we don't filter and separate stuff....
			runnable.run();
			return;
		}
		Response original = RequestCycle.get().setResponse(responseFilterMap.get(filter.getName()));
		try
		{
			runnable.run();
		}
		catch (RuntimeException ex)
		{
			throw ex;
		}
		finally
		{
			RequestCycle.get().setResponse(original);
		}
	}

}
