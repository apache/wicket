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
package org.apache.wicket.examples.resourcedecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.examples.resourcedecoration.GroupedAndOrderedResourceReference.ResourceGroup;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WicketEventReference;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.aggregation.AbstractResourceAggregatingHeaderResponse;
import org.apache.wicket.resource.aggregation.ResourceReferenceAndStringData;

/**
 * A {@link IHeaderResponse} decorator that groups the resources by type (css or js) and by
 * {@link ResourceGroup custom groups}
 * 
 * @author jthomerson
 */
public class GroupingHeaderResponse
	extends
	AbstractResourceAggregatingHeaderResponse<HttpAggregatingResourceReferenceCollection, BasicGroupingKey>
{

	private static final BasicGroupingKey UNKNOWN_CSS_GROUPING_KEY = new BasicGroupingKey(
		ResourceGroup.UNKNOWN, 0, true);
	private static final BasicGroupingKey UNKNOWN_JS_GROUPING_KEY = new BasicGroupingKey(
		ResourceGroup.UNKNOWN, 0, false);

	// holder for all javascript blocks so that we can render them *after* the script tags
	private final List<Runnable> javascriptResponse = new ArrayList<Runnable>();

	/**
	 * Construct.
	 * 
	 * @param real
	 */
	public GroupingHeaderResponse(IHeaderResponse real)
	{
		super(real);
	}

	@Override
	protected void renderCollection(Set<ResourceReferenceAndStringData> alreadyRendered,
		BasicGroupingKey key, HttpAggregatingResourceReferenceCollection coll)
	{
		// for debugging:
		getRealResponse().renderString("<!-- " + key + " -->\n");

		// TODO: I'm not sure why yet, but our aggregator fails on wicket-event.js, so for now, we
		// skip aggregating all "UNKNOWN" references
		if (ResourceGroup.UNKNOWN.equals(key.getGroup()))
		{
			super.renderCollection(alreadyRendered, key, coll);
			return;
		}

		// build an aggregated URL:
		StringBuilder refs = new StringBuilder();
		boolean first = true;
		for (ResourceReferenceAndStringData data : coll)
		{
			ResourceReference ref = data.getReference();
			if (!first)
			{
				refs.append('|');
			}
			refs.append(ref.getScope().getName()).append(':').append(ref.getName());
			first = false;
		}

		PageParameters parameters = new PageParameters();
		parameters.add("refs", refs);
		parameters.add("type", key.isCss() ? "css" : "js");
		ResourceReference ref = new PackageResourceReference("merged-resources");
		if (key.isCss())
		{
			getRealResponse().renderCSSReference(ref, parameters, null);
		}
		else
		{
			getRealResponse().renderJavaScriptReference(ref, parameters, null);
		}
	}

	@Override
	protected void onAllCollectionsRendered(
		List<ResourceReferenceAndStringData> allTopLevelReferences)
	{
		super.onAllCollectionsRendered(allTopLevelReferences);

		// you could also externalize these JS statements into a file that is loaded rather
		// than being inline in the HTML if you so desired
		for (Runnable runnable : javascriptResponse)
		{
			runnable.run();
		}
		javascriptResponse.clear();
	}

	@Override
	protected HttpAggregatingResourceReferenceCollection newResourceReferenceCollection(
		BasicGroupingKey key)
	{
		return new HttpAggregatingResourceReferenceCollection();
	}

	@Override
	protected BasicGroupingKey newGroupingKey(ResourceReferenceAndStringData ref)
	{
		// this is just a simple example. in reality, you'll almost surely want to also group by
		// media type for CSS.
		if (ref.getReference() instanceof GroupedAndOrderedResourceReference)
		{
			GroupedAndOrderedResourceReference ourRef = (GroupedAndOrderedResourceReference)ref.getReference();
			return new BasicGroupingKey(ourRef.getGroup(), ourRef.getLoadOrder(), ref.isCss());
		}
		return ref.isCss() ? UNKNOWN_CSS_GROUPING_KEY : UNKNOWN_JS_GROUPING_KEY;
	}

	@Override
	public void renderJavaScript(final CharSequence javascript, final String id)
	{
		toJsResponse(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderJavaScript(javascript, id);
			}
		});
	}

	@Override
	public void renderOnDomReadyJavaScript(final String javascript)
	{
		super.renderJavaScriptReference(WicketEventReference.INSTANCE);
		toJsResponse(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderOnDomReadyJavaScript(javascript);
			}
		});
	}

	@Override
	public void renderOnEventJavaScript(final String target, final String event,
		final String javascript)
	{
		super.renderJavaScriptReference(WicketEventReference.INSTANCE);
		toJsResponse(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderOnEventJavaScript(target, event, javascript);
			}
		});
	}

	@Override
	public void renderOnLoadJavaScript(final String javascript)
	{
		super.renderJavaScriptReference(WicketEventReference.INSTANCE);
		toJsResponse(new Runnable()
		{
			public void run()
			{
				getRealResponse().renderOnLoadJavaScript(javascript);
			}
		});
	}

	private void toJsResponse(Runnable runnable)
	{
		javascriptResponse.add(runnable);
	}

}
