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

import org.apache.wicket.PageParameters;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.resource.StringBufferResourceStream;
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
		private TextArea templateTextArea;

		/**
		 * Construct.
		 * 
		 * @param name
		 *            component name
		 */
		public TemplateForm(String name)
		{
			super(name);
			add(templateTextArea = new TextArea("templateInput", new PropertyModel(new Model(
					TemplatePage.this), "template")));
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		protected void onSubmit()
		{
		}
	}

	/** the current template contents. */
	private StringBufferResourceStream template = new StringBufferResourceStream();
	/** context to be used by the template. */
	private final Model templateContext;

	{
		template.append("<fieldset>\n");
		template.append(" <legend>persons</legend>\n");
		template.append("  <ul>\n");
		template.append("   #foreach( $person in $persons )\n");
		template.append("    <li>\n");
		template.append("     ${person.lastName},\n");
		template.append("     ${person.firstName}\n");
		template.append("    </li>\n");
		template.append("  #end\n");
		template.append(" </ul>\n");
		template.append("</fieldset>\n");
	}

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
		templateContext = Model.valueOf(map);

		add(new TemplateForm("templateForm"));
		add(new VelocityPanel("templatePanel", template, templateContext));
		add(new FeedbackPanel("feedback"));
	}

	/**
	 * Gets the current template contents.
	 * 
	 * @return the current template contents
	 */
	public final String getTemplate()
	{
		return template.asString();
	}

	/**
	 * Sets the current template contents.
	 * 
	 * @param template
	 *            the current template contents
	 */
	public final void setTemplate(String template)
	{
		this.template.clear();
		this.template.append(template);
	}
}