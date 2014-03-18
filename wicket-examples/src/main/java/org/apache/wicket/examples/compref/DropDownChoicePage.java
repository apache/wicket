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

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.io.IClusterable;


/**
 * Page with examples on {@link org.apache.wicket.markup.html.form.DropDownChoice}.
 * 
 * @author Eelco Hillenius
 */
public class DropDownChoicePage extends WicketExamplePage
{
	/** available sites for selection. */
	private static final List<String> SITES = Arrays.asList("The Server Side", "Java Lobby", "Java.Net");

	/** available numbers for selection. */
	private static final List<Integer> INTEGERS = Arrays.asList(1, 2, 3);

	/**
	 * Constructor
	 */
	public DropDownChoicePage()
	{
		final Input input = new Input();
		setDefaultModel(new CompoundPropertyModel<>(input));

		// Add a FeedbackPanel for displaying our messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);

		// Add a form with an onSumbit implementation that sets a message
		Form<?> form = new Form("form")
		{
			@Override
			protected void onSubmit()
			{
				info("input: " + input);
			}
		};
		add(form);

		// Add a dropdown choice component that uses Input's 'site' property to
		// designate the
		// current selection, and that uses the SITES list for the available
		// options.
		// Note that when the selection is null, Wicket will lookup a localized
		// string to
		// represent this null with key: "id + '.null'". In this case, this is
		// 'site.null'
		// which can be found in DropDownChoicePage.properties
		form.add(new DropDownChoice<>("site", SITES));

		// Allthough the default behavior of displaying the string
		// representations of the choices
		// by calling toString on the object might be alright in some cases, you
		// usually want to have
		// more control over it. You achieve this by providing an instance of
		// IChoiceRenderer,
		// like the example below. Don't forget to check out the default
		// implementation of
		// IChoiceRenderer, ChoiceRenderer.
		form.add(new DropDownChoice<Integer>("integer", INTEGERS, new ChoiceRenderer<Integer>()
		{
			/**
			 * Gets the display value that is visible to the end user.
			 * 
			 * @see org.apache.wicket.markup.html.form.ChoiceRenderer#getDisplayValue(java.lang.Object)
			 */
			@Override
			public Object getDisplayValue(Integer value)
			{
				// Use an ugly switch statement. Usually you would hide this in
				// your business
				// object or in a utility.
				switch (value)
				{
					case 1 :
						return "One";
					case 2 :
						return "Two";
					case 3 :
						return "Three";
					default :
						throw new IllegalStateException(value + " is not mapped!");
				}
			}

			/**
			 * Gets the value that is invisible to the end user, and that is used as the selection
			 * id.
			 * 
			 * @see org.apache.wicket.markup.html.form.ChoiceRenderer#getIdValue(java.lang.Object,
			 *      int)
			 */
			@Override
			public String getIdValue(Integer object, int index)
			{
				// though we could do kind of the reverse of what we did in
				// getDisplayValue,
				// just using your index in the list of provided options is
				// usually more
				// convenient
				return String.valueOf(INTEGERS.get(index));
			}
		}));
	}

	/** Simple data class that acts as a model for the input fields. */
	private static class Input implements IClusterable
	{
		/** the selected site. */
		public String site;

		/** the selected integer. */
		public Integer integer = INTEGERS.get(0);

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "site = '" + site + "', integer = " + integer;
		}
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<select wicket:id=\"site\">\n" + "    <option>site 1</option>\n"
			+ "    <option>site 2</option>\n" + "</select>\n" + "<select wicket:id=\"integer\">\n"
			+ "    <option>Fifty</option>\n" + "    <option>Sixty</option>\n" + "</select>";
		String code = "/** available sites for selection. */\n"
			+ "private static final List SITES = Arrays.asList(new String[] {\"The Server Side\", \"Java Lobby\", \"Java.Net\" });\n"
			+ "/** available numbers for selection. */\n"
			+ "private static final List INTEGERS = Arrays.asList(new Integer[] { new Integer(1), new Integer(2), new Integer(3) });\n"
			+ "\n"
			+ "public DropDownChoicePage() {\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;...\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// Add a dropdown choice component that uses the model object's 'site' property to designate the\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// current selection, and that uses the SITES list for the available options.\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// Note that when the selection is null, Wicket will lookup a localized string to\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// represent this null with key: \"id + '.null'\". In this case, this is 'site.null'\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// which can be found in DropDownChoicePage.properties\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(new DropDownChoice(\"site\", SITES));\n"
			+ "\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// Allthough the default behavior of displaying the string representations of the choices\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// by calling toString on the object might be alright in some cases, you usually want to have\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// more control over it. You achieve this by providing an instance of IChoiceRenderer.\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// Don't forget to check out the default implementation of IChoiceRenderer, ChoiceRenderer.\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(new DropDownChoice(\"integer\", INTEGERS, new IChoiceRenderer() {\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Gets the display value that is visible to the end user.\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;public String getDisplayValue(Object object) {\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}\n"
			+ "\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Gets the value that is invisble to the end user, and that is used as the selection id.\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;public String getIdValue(Object object, int index) {\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;}));\n" + "}";
		add(new ExplainPanel(html, code));

	}

}
