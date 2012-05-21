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
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryPluginResourceReference;

/**
 * @author papegaaij
 */
public class FilteredHeaderPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parameters
	 */
	public FilteredHeaderPage(final PageParameters parameters)
	{
		super(parameters);

		/*
		 * a container for all collected JavaScript contributions that will be loaded at the page
		 * footer (after </body>)
		 */
		add(new HeaderResponseContainer("footerJS", "footerJS"));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		// rendered at the default header bucket
		final JavaScriptResourceReference topJsReference = new JavaScriptResourceReference(
			FilteredHeaderPage.class, "top.js");
		response.render(new FilteredHeaderItem(JavaScriptHeaderItem.forReference(topJsReference),
			JavaScriptFilteredIntoFooterHeaderResponse.HEADER_FILTER_NAME));

		// rendered at the bottom of the body bucket
		JQueryPluginResourceReference bottomJs = new JQueryPluginResourceReference(
			FilteredHeaderPage.class, "bottom.js")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Iterable<? extends HeaderItem> getDependencies()
			{
				Iterable<? extends HeaderItem> dependencies = super.getDependencies();
				List<HeaderItem> deps = new ArrayList<HeaderItem>();
				for (HeaderItem hi : dependencies)
				{
					deps.add(hi);
				}
				// WICKET-4566 : depend on a resource which is rendered in a different bucket
				deps.add(JavaScriptHeaderItem.forReference(topJsReference));
				return deps;
			}
		};
		response.render(JavaScriptHeaderItem.forReference(bottomJs));
	}
}
