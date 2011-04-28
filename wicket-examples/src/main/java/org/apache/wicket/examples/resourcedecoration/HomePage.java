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
import org.apache.wicket.examples.resourcedecoration.GroupedAndOrderedResourceReference.ResourceGroup;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.resource.filtering.HeaderResponseFilteredResponseContainer;
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
				jsPlaceholder.setOutputMarkupId(true);
				response.renderOnDomReadyJavaScript("$('#" + jsPlaceholder.getMarkupId() +
					"').html('the ondomready script ran').css('border-color', 'green');");
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
				stop();
			}
		});

		/*
		 * a container for all collected JavaScript contributions that will be loaded at the page
		 * footer (after </body>)
		 */
		add(new HeaderResponseFilteredResponseContainer("footerJS", "footerJS"));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		// example of things that may be shared for all your applications across your company,

		// two CSS resources in the same group. header.css is rendered first because has lower
		// "order" number
		response.renderCSSReference(new GroupedAndOrderedResourceReference(ResourceGroup.GLOBAL, 0,
			HomePage.class, "footer.css"));
		response.renderCSSReference(new GroupedAndOrderedResourceReference(ResourceGroup.GLOBAL, 0,
			HomePage.class, "header.css"));

		response.renderJavaScriptReference(new GroupedAndOrderedResourceReference(
			ResourceGroup.GLOBAL, 0, HomePage.class, "jquery-1.4.3.min.js"));

		// example of something that may be in this single application:
		response.renderCSSReference(new GroupedAndOrderedResourceReference(
			ResourceGroup.APPLICATION, 0, HomePage.class, "app.css"));

		// example of something that may be limited to certain pages:
		response.renderCSSReference(new GroupedAndOrderedResourceReference(ResourceGroup.PAGE, 0,
			HomePage.class, "HomePage.css"));
		response.renderJavaScriptReference(new GroupedAndOrderedResourceReference(
			ResourceGroup.PAGE, 0, HomePage.class, "HomePage.js"));
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
			if (AjaxRequestTarget.get() != null)
			{
				response.renderCSSReference(new PackageResourceReference(HomePage.class, "ajax.css"));
				response.renderJavaScriptReference(new PackageResourceReference(HomePage.class,
					"ajax.js"));
				response.renderOnDomReadyJavaScript("updatePending();");
			}
		}
	}
}
