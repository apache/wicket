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

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;


/**
 * Page with examples on {@link org.apache.wicket.markup.html.form.Form}.
 * 
 * @author Eelco Hillenius
 */
public class FormPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public FormPage()
	{
		// Add a FeedbackPanel for displaying our messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);

		// Add a form with an onSubmit implementation that sets a message
		add(new Form<Void>("form")
		{
			@Override
			protected void onSubmit()
			{
				info("the form was submitted!");
			}
		});
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<form wicket:id=\"form\">\n"
			+ "<input type=\"submit\" value=\"click me to submit the form and display a message\" />\n"
			+ "</form>\n" + "<span wicket:id=\"feedback\">feedbackmessages will be put here</span>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;public FormPage() {\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// Add a FeedbackPanel for displaying our messages\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;FeedbackPanel feedbackPanel = new FeedbackPanel(\"feedback\");\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add(feedbackPanel);\n"
			+ "\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// Add a form with an onSubmit implementation that sets a message\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add(new Form(\"form\") {\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;protected void onSubmit() {\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;info(\"the form was submitted!\");\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;});\n" + "&nbsp;&nbsp;&nbsp;&nbsp;}";
		add(new ExplainPanel(html, code));

	}

}
