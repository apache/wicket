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
package org.apache.wicket.request.cycle;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormValidatingBehavior;
import org.apache.wicket.markup.head.CssContentHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;

@SuppressWarnings("javadoc")
public class RerenderAjaxPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	public static final String HEAD_TEXT = "#invisible { display:none; }\nbody {background-color:#ccc; }\n";

	private FeedbackPanel feedback;

	public RerenderAjaxPage(final PageParameters parameters)
	{
		super(parameters);

		// the page needs a component that is refreshed using AJAX, so a feedback panel will do
		// nicely
		feedback = new FeedbackPanel("feedback");
		add(feedback.setOutputMarkupPlaceholderTag(true));

		Form<Void> form = new Form<Void>("form");
		add(form);
		TextField<String> username = new TextField<String>("username", Model.of(""));

		// make it so we can never ever have a successful submit
		username.add(StringValidator.minimumLength(Integer.MAX_VALUE));
		form.add(username.setRequired(true));

		// add an AJAX event to the text field so we can trigger the WICKET-5960 AJAX render bug
		username.add(new AjaxFormValidatingBehavior("blur")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				super.onError(target);

				// add the component to the AJAX envelope so that we trigger a
				// PartialHtmlHeaderContainer to be set on the page.
				target.add(feedback);
			}
		});
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		// and add our own special sauce to the page to emphasize the issue. If this CSS is not
		// rendered, the page will show a big red box telling you about the failure.
		response.render(new CssContentHeaderItem(HEAD_TEXT, "mystyle", null));
	}
}
