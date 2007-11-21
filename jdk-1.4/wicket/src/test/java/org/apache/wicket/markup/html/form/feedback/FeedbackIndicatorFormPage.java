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
package org.apache.wicket.markup.html.form.feedback;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.FormComponentFeedbackIndicator;
import org.apache.wicket.model.Model;

/**
 * @author jcompagner
 */
public class FeedbackIndicatorFormPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public FeedbackIndicatorFormPage()
	{
		final TextField tf = new TextField("input", new Model());
		final FormComponentFeedbackIndicator feedback = new FormComponentFeedbackIndicator(
			"feedback");

		Form form = new Form("form")
		{
			private static final long serialVersionUID = 1L;

			protected void onSubmit()
			{
				tf.error("an error");
			}
		};
		form.add(feedback);
		form.add(tf);

		add(form);
	}
}
