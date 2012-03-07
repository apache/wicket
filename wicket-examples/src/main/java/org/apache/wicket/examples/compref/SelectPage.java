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
package org.apache.wicket.examples.compref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.WildcardCollectionModel;


/**
 * Page with examples on {@link org.apache.wicket.extensions.markup.html.form.select.Select}.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Eelco Hillenius
 */
public class SelectPage extends WicketExamplePage
{
	/** available sites for selection. */
	private static final List<String> SITES = Arrays.asList("The Server Side", "Java Lobby",
		"Java.Net");

	/** available choices for large selection box. */
	private static final List<? extends String> MANY_CHOICES = Arrays.asList("Choice1", "Choice2",
		"Choice3", "Choice4", "Choice5", "Choice6", "Choice7", "Choice8", "Choice9");

	/**
	 * Constructor
	 */
	public SelectPage()
	{
		final Input input = new Input();
		setDefaultModel(new CompoundPropertyModel<Input>(input));

		// Add a FeedbackPanel for displaying our messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);

		// Add a form with an onSubmit implementation that sets a message
		Form<?> form = new Form<Void>("form")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				info("input: " + input);
			}
		};
		add(form);

		Select<String> site = new Select<String>("site");
		form.add(site);
		site.add(new SelectOption<String>("site1", new Model<String>("tss")));
		site.add(new SelectOption<String>("site2", new Model<String>("jl")));
		site.add(new SelectOption<String>("site3", new Model<String>("sd")));
		site.add(new SelectOption<String>("site4", new Model<String>("bn")));

		Select<List<String>> choices = new Select<List<String>>("choices");
		form.add(choices);
		IOptionRenderer<String> renderer = new IOptionRenderer<String>()
		{
			private static final long serialVersionUID = 1L;

			public String getDisplayValue(String object)
			{
				return object;
			}

			public IModel<String> getModel(String value)
			{
				return new Model<String>(value);
			}

		};
		IModel<Collection<? extends String>> model = new WildcardCollectionModel<String>(
			MANY_CHOICES);
		choices.add(new SelectOptions<String>("manychoices", model, renderer));

	}

	/** Simple data class that acts as a model for the input fields. */
	private static class Input implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		/** the selected sites. */
		public String site = "sd";

		/** the selected choices. */
		public List<String> choices = new ArrayList<String>();

		/** adds pre-selected items to the choices list */
		public Input()
		{
			choices.add("Choice3");
			choices.add("Choice4");
			choices.add("Choice5");
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "site = '" + site + "', choices='" + listAsString(choices) + "'";
		}

		private String listAsString(List<String> list)
		{
			StringBuilder b = new StringBuilder();
			for (Iterator<String> i = list.iterator(); i.hasNext();)
			{
				b.append(i.next());
				if (i.hasNext())
				{
					b.append(", ");
				}
			}
			return b.toString();
		}
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<select wicket:id=\"sites\">\n" + "    <option>site 1</option>\n"
			+ "    <option>site 2</option>\n" + "</select>\n" + "<select wicket:id=\"choices\">\n"
			+ "    <option>choice 1</option>\n" + "    <option>choice 2</option>\n" + "</select>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;// Add a multiple list choice component that uses the model object's 'site'\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// property to designate the current selection, and that uses the SITES\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// list for the available options.\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// Note that our model here holds a Collection, as we need to store\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// multiple values too\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;ListMultipleChoice siteChoice = new ListMultipleChoice(\"sites\", SITES);\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(siteChoice);\n"
			+ "\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;ListMultipleChoice manyChoice = new ListMultipleChoice(\"choices\", MANY_CHOICES).setMaxRows(5);\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(manyChoice);";

		html = "SEE INSIDE FOR NOW";
		code = "SEE INSIDE FOR NOW";

		add(new ExplainPanel(html, code));

	}
}