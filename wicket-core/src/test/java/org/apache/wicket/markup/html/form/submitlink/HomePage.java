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
package org.apache.wicket.markup.html.form.submitlink;

import static junit.framework.Assert.*;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

/**
 */
public class HomePage extends WebPage
{
	boolean submitted = false;
	boolean submittedViaLink = false;
	boolean submittedViaLinkAfter = false;
	String text;
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public HomePage()
	{
		super();
		Form<Void> form = new Form<Void>("form")
		{
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				submitted = true;
			}
		};
		form.add(new TextField<String>("text", new PropertyModel<String>(HomePage.this, "text")));
		form.add(new SubmitLink("link")
		{

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;


			@Override
			public void onSubmit()
			{
				submittedViaLink = true;
				assertFalse("after must not yet have been called", submittedViaLinkAfter);
			}

			@Override
			public void onSubmitAfterForm()
			{
				assertTrue("onsubmit must have been called!", submittedViaLink);
				submittedViaLinkAfter = true;
			}
		});

		add(form);
	}

	/**
	 * @return submitted
	 */
	public boolean isSubmitted()
	{
		return submitted;
	}

	/**
	 * @return text
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * @param text
	 */
	public void setText(String text)
	{
		this.text = text;
	}

	/**
	 * @return submittedViaLink
	 */
	public boolean isSubmittedViaLink()
	{
		return submittedViaLink;
	}

	boolean isSubmittedViaLinkAfter()
	{
		return submittedViaLinkAfter;
	}
}
