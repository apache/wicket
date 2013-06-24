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

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;


/**
 * Page with examples on {@link org.apache.wicket.markup.html.form.TextArea}.
 * 
 * @author Eelco Hillenius
 */
public class TextAreaPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public TextAreaPage()
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

		// add a text area component that uses Input's 'text' property.
		form.add(new TextArea<>("text"));
	}

	/** Simple data class that acts as a model for the input fields. */
	private static class Input implements IClusterable
	{
		/** some plain text. */
		public String text = "line 1\nline 2\nline 3";

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "text = '" + text + "'";
		}
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<textarea wicket:id=\"text\" rows=\"6\" cols=\"20\">Input comes here</textarea>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;// add a text area component that uses the model object's 'text' property.\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(new TextArea(\"text\"));";
		add(new ExplainPanel(html, code));
	}

}