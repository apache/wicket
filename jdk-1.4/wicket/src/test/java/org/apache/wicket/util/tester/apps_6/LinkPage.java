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
package org.apache.wicket.util.tester.apps_6;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Different kinds of links, to be test the {@link WicketTester#clickLink(String, boolean)} method.
 * 
 * Add more links when needed.
 */
public class LinkPage extends WebPage
{
	/**
	 * Construct.
	 */
	public LinkPage()
	{
		// Link
		add(new Link("linkWithSetResponsePageClass")
		{
			private static final long serialVersionUID = 1L;

			public void onClick()
			{
				getRequestCycle().setResponsePage(ResultPage.class);
			}
		});

		add(new Link("linkWithSetResponsePage")
		{
			private static final long serialVersionUID = 1L;

			public void onClick()
			{
				getRequestCycle().setResponsePage(new ResultPage("A special label"));
			}
		});

		// AjaxLink
		add(new AjaxLink("ajaxLinkWithSetResponsePageClass")
		{
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target)
			{
				getRequestCycle().setResponsePage(ResultPage.class);
			}
		});

		add(new AjaxLink("ajaxLinkWithSetResponsePage")
		{
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target)
			{
				getRequestCycle().setResponsePage(new ResultPage("A special label"));
			}
		});

		// AjaxFallbackLink
		add(new AjaxFallbackLink("ajaxFallbackLinkWithSetResponsePageClass")
		{
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target)
			{
				getRequestCycle().setResponsePage(ResultPage.class);
			}
		});

		add(new AjaxFallbackLink("ajaxFallbackLinkWithSetResponsePage")
		{
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target)
			{
				getRequestCycle().setResponsePage(new ResultPage("A special label"));
			}
		});

		// AjaxSubmitLink
		final Form form = new Form("form");
		add(form);
		final AjaxSubmitLink submit = new AjaxSubmitLink("submit")
		{
			private static final long serialVersionUID = 1L;

			protected void onSubmit(final AjaxRequestTarget target, final Form form)
			{
				getRequestCycle().setResponsePage(new ResultPage("A form label"));
			}
		};
		form.add(submit);
	}
}
