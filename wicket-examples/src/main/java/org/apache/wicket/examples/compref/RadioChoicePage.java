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

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;


/**
 * Page with examples on {@link org.apache.wicket.markup.html.form.ListChoice}.
 * 
 * @author Eelco Hillenius
 */
public class RadioChoicePage extends WicketExamplePage
{
	/** available sites for selection. */
	private static final List<String> SITES = Arrays.asList("The Server Side", "Java Lobby", "Java.Net");

	/**
	 * Constructor
	 */
	public RadioChoicePage()
	{
		final Input input = new Input();
		setDefaultModel(new CompoundPropertyModel<>(input));

		// Add a FeedbackPanel for displaying our messages
		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
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

		// Add a radio choice component that uses Input's 'site' property to
		// designate the
		// current selection, and that uses the SITES list for the available
		// options.
		RadioChoice<String> sites = new RadioChoice<>("site", SITES);
		sites.add(new AjaxFormChoiceComponentUpdatingBehavior()
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				info("Selected: " + getComponent().getDefaultModelObjectAsString());
				target.add(feedbackPanel);
			}
		});
		form.add(sites);
	}

	/** Simple data class that acts as a model for the input fields. */
	private static class Input implements IClusterable
	{
		/** the selected site. */
		public String site = SITES.get(0);

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "site = '" + site + "'";
		}
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<span valign=\"top\" wicket:id=\"site\">\n"
			+ "  <input type=\"radio\">site 1</input>\n"
			+ "  <input type=\"radio\">site 2</input>\n" + "</span>";
		String code = "private static final List SITES = Arrays.asList(new String[] { \"The Server Side\", \"Java Lobby\", \"Java.Net\" });\n"
			+ "...\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// Add a radio choice component that uses the model object's 'site' property to designate the\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;// current selection, and that uses the SITES list for the available options.\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(new RadioChoice(\"site\", SITES));";
		add(new ExplainPanel(html, code));

	}

}