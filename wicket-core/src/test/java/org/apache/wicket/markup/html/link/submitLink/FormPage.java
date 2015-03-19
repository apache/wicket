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
package org.apache.wicket.markup.html.link.submitLink;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

/**
 *
 */
public class FormPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private final Integer somevalue = 1;

	private boolean formSubmitted;

	private boolean submitLinkSubmitted;

	private final Form<Void> form;

	/**
	 * Construct.
	 */
	public FormPage()
	{
		form = new Form<Void>("form")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				formSubmitted = true;
			}
		};
		add(form);

		form.add(new TextField<Integer>("field", new PropertyModel<Integer>(this, "somevalue")));

		add(new SubmitLink("link", form)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit()
			{
				submitLinkSubmitted = true;
			}
		});
	}

	/**
	 * @return formSubmitted
	 */
	public boolean isFormSubmitted()
	{
		return formSubmitted;
	}

	/**
	 * @return submitLinkSubmitted
	 */
	public boolean isSubmitLinkSubmitted()
	{
		return submitLinkSubmitted;
	}
}
