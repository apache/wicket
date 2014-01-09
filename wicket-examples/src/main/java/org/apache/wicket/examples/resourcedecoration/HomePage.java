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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.filter.FilteredHeaderItem;
import org.apache.wicket.markup.head.filter.FilteringHeaderResponse;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.resource.JQueryPluginResourceReference;
import org.apache.wicket.util.time.Duration;

/**
 * A demo page showing how to render grouped resources
 * 
 * @author jthomerson
 */
public class HomePage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parameters
	 */
	public HomePage(final PageParameters parameters)
	{
		super(parameters);

		final WebMarkupContainer jsPlaceholder = new WebMarkupContainer("jsProofPlaceholder");
		jsPlaceholder.add(new Behavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void renderHead(Component component, IHeaderResponse response)
			{
				response.render(OnDomReadyHeaderItem.forScript("$('#" +
					jsPlaceholder.getMarkupId() +
					"').html('the ondomready script ran').css('border-color', 'green');"));
			}
		});
		add(jsPlaceholder);

		add(new AjaxProofContainer("ajaxProofPlaceholder"));
		add(new AbstractAjaxTimerBehavior(Duration.seconds(4))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onTimer(AjaxRequestTarget target)
			{
				HomePage.this.replace(new AjaxProofContainer("ajaxProofPlaceholder"));
				target.add(HomePage.this.get("ajaxProofPlaceholder"));
				stop(target);
			}
		});

		/*
		 * a container for all collected JavaScript contributions that will be loaded at the page
		 * footer (after </body>)
		 */
		add(new HeaderResponseContainer("footerJS", "footerJS"));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		// example of things that may be shared for all your applications across your company,

		// two CSS resources in the same group. header.css is rendered first because has lower
		// "order" number
		response.render(CssHeaderItem.forReference(new CssResourceReference(HomePage.class,
			"footer.css")));
		response.render(CssHeaderItem.forReference(new CssResourceReference(HomePage.class,
			"header.css")));

		// example of something that may be in this single application:
		response.render(CssHeaderItem.forReference(new CssResourceReference(HomePage.class,
			"app.css")));

		// example of something that may be limited to certain pages:
		response.render(CssHeaderItem.forReference(new CssResourceReference(HomePage.class,
			"HomePage.css")));
		response.render(JavaScriptHeaderItem.forReference(new JQueryPluginResourceReference(
			HomePage.class, "HomePage.js")));
		response.render(new FilteredHeaderItem(
			JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(HomePage.class,
				"top.js")), FilteringHeaderResponse.DEFAULT_HEADER_FILTER_NAME));
	}

	private static class AjaxProofContainer extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		public AjaxProofContainer(String id)
		{
			super(id);
			setOutputMarkupId(true);
		}

		@Override
		public void renderHead(IHeaderResponse response)
		{
			if (getRequestCycle().find(AjaxRequestTarget.class) != null)
			{
				response.render(CssHeaderItem.forReference(new PackageResourceReference(
					HomePage.class, "ajax.css")));
				response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(
					HomePage.class, "ajax.js")));
				response.render(OnDomReadyHeaderItem.forScript("updatePending();"));
			}
		}
	}
}
