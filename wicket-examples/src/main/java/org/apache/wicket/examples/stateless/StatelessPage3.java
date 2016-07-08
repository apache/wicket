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
package org.apache.wicket.examples.stateless;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.RangeValidator;

/**
 * Another page of the stateless example.
 * 
 * @author Eelco Hillenius
 * @author Matej Knopp
 */
public class StatelessPage3 extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	private Integer number;

	private static final String PARAMETER_NAME = "value";

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();

		// get the value from page parameters
		number = getPage().getPageParameters().get(PARAMETER_NAME).toInt(10);
	}

	/**
	 * Constructor
	 */
	public StatelessPage3()
	{
		setStatelessHint(true);
		add(new Label("message", new SessionModel()));
		add(new BookmarkablePageLink<>("indexLink", Index.class));
		final TextField<Integer> field = new TextField<>("textfield",
			new PropertyModel<Integer>(this, "number"));
		field.add(new RangeValidator<>(0, 20));
		field.setRequired(true);

		StatelessForm<?> statelessForm = new StatelessForm<Void>("statelessform")
		{
			@Override
			protected void onSubmit()
			{
				info("Submitted text: " + field.getDefaultModelObject());

				// store the value in page parameters
				getPage().getPageParameters().set(PARAMETER_NAME, number);
			}

		};
		statelessForm.add(field);
		add(statelessForm);
		add(new FeedbackPanel("feedback"));
	}

}
