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
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;


/**
 * Page with examples on {@link org.apache.wicket.markup.html.form.Form}.
 * 
 * @author Eelco Hillenius
 */
public class SubmitLinkPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public SubmitLinkPage()
	{
		// Add a FeedbackPanel for displaying our messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);

		// Add a form with 2 SubmitLinks that can be called
		Form<?> form = new Form("form");
		add(form);

		SubmitLink internal = new SubmitLink("internal")
		{
			@Override
			public void onSubmitBeforeForm()
			{
				info("internal onsubmit");
			}
		};
		form.add(internal);

		SubmitLink external = new SubmitLink("external", form)
		{
			@Override
			public void onSubmitBeforeForm()
			{
				info("external onsubmit");
			}
		};
		add(external);
	}

	/**
	 * Override base method to provide an explanation
	 */
	@Override
	protected void explain()
	{
		String html = "<form wicket:id=\"form\">\n"
			+ "<a wicket:id=\"internal\">Internal SubmitLink</a>\n" + "</form>\n"
			+ "<a wicket:id=\"external\">External SubmitLink</a>\n";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;public SubmitLinkPage() {\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// Add a FeedbackPanel for displaying our messages\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;FeedbackPanel feedbackPanel = new FeedbackPanel(\"feedback\");\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add(feedbackPanel);\n"
			+ "\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;// Add a form with 2 SubmitLinks that can be called\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Form form = new Form(\"form\");\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add(form);\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;SubmitLink internal = new SubmitLink(\"internal\");\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;form.add(internal);\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;SubmitLink external = new SubmitLink(\"external\", form);\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add(external);\n"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;});\n" + "&nbsp;&nbsp;&nbsp;&nbsp;}";
		add(new ExplainPanel(html, code));

	}
}
