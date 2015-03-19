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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;


/**
 * Mock page for testing.
 * 
 * @author Juergen Donnerstag
 */
public class EnclosurePage_9 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/** */
	public boolean formValidate;

	/** */
	public boolean inputOnBeforeRender;

	/** */
	public boolean inputValidate;

	/** */
	public boolean labelOnBeforeRender;

	/** */
	public boolean checkbox;

	/**
	 * Construct.
	 */
	public EnclosurePage_9()
	{
		Form<Void> form = new Form<Void>("form")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onValidate()
			{
				formValidate = true;
				super.onValidate();
			}
		};

		add(form);

		form.add(new CheckBox("input", new PropertyModel<Boolean>(this, "checkbox"))
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.Component#onBeforeRender()
			 */
			@Override
			protected void onBeforeRender()
			{
				inputOnBeforeRender = true;
				super.onBeforeRender();
			}

			@Override
			public void validate()
			{
				inputValidate = true;
				super.validate();
			}
		});

		form.add(new Label("label")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeRender()
			{
				labelOnBeforeRender = true;
				super.onBeforeRender();
			};
		});
	}

	/**
	 * 
	 */
	public void reset()
	{
		inputOnBeforeRender = false;
		inputValidate = false;
		labelOnBeforeRender = false;
		formValidate = false;
	}
}
