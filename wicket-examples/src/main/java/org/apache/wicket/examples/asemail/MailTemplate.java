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
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.handler.render.PageRenderer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
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

		final Form<Void> form = new Form<Void>("form");
		add(form);

		TextField<String> nameTextField = new TextField<String>("name", new PropertyModel<String>(
			MailTemplate.this, "name"));
		nameTextField.setOutputMarkupId(true);
		form.add(nameTextField);

		final MultiLineLabel result = new MultiLineLabel("result", new Model<String>());
		result.setOutputMarkupId(true);
		add(result);

		AjaxSubmitLink basedOnPageLink = new AjaxSubmitLink("pageBasedLink", form)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				CharSequence pageHtml = renderPage(TemplateBasedOnPage.class);

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
				CharSequence panelHtml = renderPanel(new MailTemplatePanel(DummyPage.COMP_ID,
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

				CharSequence templateHtml = renderTemplate(new PackageTextTemplate(
					MailTemplate.class, "mail-template.tmpl"));
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
	 * Loads the mail template from a file with placeholders, populates them and returns the text
	 * that can be used as mail body.
	 * 
	 * @param template
	 *            the {@link TextTemplate} to use for the final result
	 * @return the fully populated template
	 */
	private CharSequence renderTemplate(final TextTemplate template)
	{
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("name", name);

		CharSequence relativeUrl = urlFor(new PackageResourceReference(MailTemplate.class,
			"resource.txt"), null);
		String href = getRequestCycle().getUrlRenderer().renderFullUrl(
			Url.parse(relativeUrl.toString()));
		variables.put("downloadLink", href);

		String html = template.asString(variables);
		return html;
	}

	/**
	 * Collects the html generated by the rendering of a page.
	 * 
	 * @param pageClass
	 *            the class of the page which should be rendered.
	 * @return the html rendered by a page
	 */
	private CharSequence renderPage(final Class<? extends Page> pageClass)
	{
		PageParameters parameters = new PageParameters();
		parameters.set("name", name);

		final RenderPageRequestHandler handler = new RenderPageRequestHandler(new PageProvider(
			pageClass, parameters), RedirectPolicy.NEVER_REDIRECT);

		final PageRenderer pageRenderer = getApplication().getPageRendererProvider().get(handler);

		RequestCycle originalRequestCycle = getRequestCycle();

		BufferedWebResponse tempResponse = new BufferedWebResponse(null);

		RequestCycleContext requestCycleContext = new RequestCycleContext(originalRequestCycle.getRequest(),
				tempResponse, getApplication().getRootRequestMapper(), getApplication().getExceptionMapperProvider().get());
		RequestCycle tempRequestCycle = new RequestCycle(requestCycleContext);

		final Response oldResponse = originalRequestCycle.getResponse();

		try
		{
			originalRequestCycle.setResponse(tempResponse);
			pageRenderer.respond(tempRequestCycle);
		}
		finally
		{
			originalRequestCycle.setResponse(oldResponse);
		}

		return tempResponse.getText();
	}

	/**
	 * Collects the html generated by the rendering of a page.
	 * 
	 * @param panel
	 *            the panel that should be rendered.
	 * @return the html rendered by the panel
	 */
	private CharSequence renderPanel(final Panel panel)
	{
		RequestCycle requestCycle = getRequestCycle();

		final Response oldResponse = requestCycle.getResponse();
		BufferedWebResponse tempResponse = new BufferedWebResponse(null);

		try
		{
			requestCycle.setResponse(tempResponse);

			DummyPage page = new DummyPage();
			page.add(panel);

			panel.render();
		}
		finally
		{
			requestCycle.setResponse(oldResponse);
		}

		return tempResponse.getText();
	}

	/**
	 * A page used as a parent for the panel based templating.
	 */
	private static class DummyPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private static final String COMP_ID = "dummy";

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<wicket:container wicket:id='" + COMP_ID +
				"'></wicket:container>");
		}

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
