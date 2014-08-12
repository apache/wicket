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
package org.apache.wicket.examples.asemail;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.util.string.ComponentRenderer;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;

/**
 * An example page that shows how to generate email templates from a Wicket {@link Page}, a
 * {@link Panel} or a {@link TextTemplate}
 */
public class MailTemplate extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	/**
	 * The name of the customer for which a mail body will be generated.
	 */
	private String name = "";

	/**
	 * Constructor.
	 * 
	 * @param parameters
	 *            the current page parameters
	 */
	public MailTemplate(final PageParameters parameters)
	{
		super(parameters);

		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);

		final Form<Void> form = new Form<>("form");
		add(form);

		TextField<String> nameTextField = new TextField<>("name", new PropertyModel<String>(
			MailTemplate.this, "name"));
		nameTextField.setOutputMarkupId(true);
		form.add(nameTextField);

		final MultiLineLabel result = new MultiLineLabel("result", new Model<>());
		result.setOutputMarkupId(true);
		add(result);

		AjaxSubmitLink basedOnPageLink = new AjaxSubmitLink("pageBasedLink", form)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				PageParameters parameters = new PageParameters();
				parameters.set("name", name);
				PageProvider pageProvider = new PageProvider(TemplateBasedOnPage. class, parameters);
				CharSequence pageHtml = ComponentRenderer.renderPage(pageProvider);

				updateResult(result, pageHtml, target);
				target.add(feedback);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				target.add(feedback);
			}
		};

		AjaxSubmitLink basedOnPanelLink = new AjaxSubmitLink("panelBasedLink", form)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				CharSequence panelHtml = ComponentRenderer.renderComponent(new MailTemplatePanel("someId",
						new PropertyModel<String>(MailTemplate.this, "name")));

				updateResult(result, panelHtml, target);
				target.add(feedback);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				target.add(feedback);
			}
		};

		AjaxSubmitLink basedOnTextTemplateLink = new AjaxSubmitLink("textTemplateBasedLink", form)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				Map<String, Object> variables = new HashMap<>();
				variables.put("name", name);

				CharSequence relativeUrl = urlFor(new PackageResourceReference(MailTemplate.class,
						"resource.txt"), null);
				String href = getRequestCycle().getUrlRenderer().renderFullUrl(
						Url.parse(relativeUrl.toString()));
				variables.put("downloadLink", href);

				PackageTextTemplate template = new PackageTextTemplate(MailTemplate.class, "mail-template.tmpl");
				CharSequence templateHtml = template.asString(variables);
				updateResult(result, templateHtml, target);
				target.add(feedback);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				target.add(feedback);
			}
		};

		add(basedOnPageLink, basedOnPanelLink, basedOnTextTemplateLink);
	}

	/**
	 * Updates the component that is used to show the generated mail body for this example.
	 * 
	 * @param result
	 *            the component that shows the mail body
	 * @param mailBody
	 *            the text for the mail body
	 * @param target
	 *            the current Ajax request handler
	 */
	private void updateResult(final Component result, final CharSequence mailBody,
		final AjaxRequestTarget target)
	{
		result.setDefaultModelObject(mailBody);
		target.add(result);
	}

}
