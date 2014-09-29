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
package org.apache.wicket.util.tester;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;


/**
 * Mock page used for testing executeAjaxEvent.
 * 
 * @author Frank Bille (billen)
 */
public class MockPageWithFormAndAjaxFormSubmitBehavior extends WebPage
{
	static final String EVENT_COMPONENT = "eventComponent";

	private static final long serialVersionUID = 1L;

	private boolean executed = false;

	private final Pojo pojo;

	/**
	 * Construct.
	 */
	public MockPageWithFormAndAjaxFormSubmitBehavior()
	{
		pojo = new Pojo("Mock name");

		Form<Pojo> form = new Form<Pojo>("form", new CompoundPropertyModel<Pojo>(pojo));
		add(form);

		form.add(new TextField<String>("name"));

		// The Event behavior
		WebComponent eventComponent = new WebComponent(EVENT_COMPONENT);
		add(eventComponent);
		eventComponent.add(new AjaxFormSubmitBehavior(form, "click")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				executed = true;
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
			}
		});
	}

	/**
	 * @return Is the onSubmit executed?
	 */
	public boolean isExecuted()
	{
		return executed;
	}

	/**
	 * @return The pojo used in the form
	 */
	public Pojo getPojo()
	{
		return pojo;
	}

	/**
	 * Pojo data object
	 */
	public static class Pojo implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String name;

		/**
		 * Construct.
		 * 
		 * @param name
		 */
		public Pojo(String name)
		{
			this.name = name;
		}

		/**
		 * @return name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * @param name
		 */
		public void setName(String name)
		{
			this.name = name;
		}
	}
}
