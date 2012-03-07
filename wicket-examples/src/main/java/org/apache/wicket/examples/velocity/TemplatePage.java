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
package org.apache.wicket.examples.velocity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.resource.ResourceUtil;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.IStringResourceStream;
import org.apache.wicket.core.util.resource.PackageResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.velocity.markup.html.VelocityPanel;

/**
 * Template example page.
 * 
 * @author Eelco Hillenius
 */
public class TemplatePage extends WicketExamplePage
{
	/**
	 * Form for changing the template contents.
	 */
	private final class TemplateForm extends Form
	{
		private TextArea<IStringResourceStream> templateTextArea;

		/**
		 * Construct.
		 * 
		 * @param name
		 *            component name
		 */
		public TemplateForm(String name)
		{
			super(name);
			add(templateTextArea = new TextArea<IStringResourceStream>("templateInput",
				new PropertyModel<IStringResourceStream>(
					new Model<TemplatePage>(TemplatePage.this), "template")));
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		protected void onSubmit()
		{
		}
	}

	/** the current template contents. */
	private IResourceStream template = new PackageResourceStream(DynamicPage.class, "persons.vm");

	/** context to be used by the template. */
	private final IModel<Map<String, List<Person>>> templateContext;

	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public TemplatePage(final PageParameters parameters)
	{
		Map<String, List<Person>> map = new HashMap<String, List<Person>>();
		map.put("persons", VelocityTemplateApplication.getPersons());
		templateContext = Model.ofMap(map);

		add(new TemplateForm("templateForm"));
		add(new VelocityPanel("templatePanel", templateContext)
		{
			@Override
			protected IResourceStream getTemplateResource()
			{
				return template;
			}
		});
		add(new FeedbackPanel("feedback"));
	}

	/**
	 * Gets the current template contents.
	 * 
	 * @return the current template contents
	 */
	public final String getTemplate()
	{
		return ResourceUtil.readString(template);
	}

	/**
	 * Sets the current template contents.
	 * 
	 * @param template
	 *            the current template contents
	 */
	public final void setTemplate(String template)
	{
		this.template = new StringResourceStream(template);
	}
}