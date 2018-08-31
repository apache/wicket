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
package org.apache.wicket.markup.html.form;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

/**
 */
public class MultiPartFormPage extends WebPage
{
	private static final long serialVersionUID = 1L;
	
	public boolean multiPart;
	
	public int asked = 0;

	public Form<?> form;

	private TextField<String> input;

	public AjaxFallbackButton button1;

	private AjaxFallbackButton button2;

	/**
	 * Construct.
	 */
	public MultiPartFormPage()
	{
		form = new Form<Void>("form");
		add(form.setOutputMarkupId(true));
		
		input = new TextField<String>("input", Model.of(""))
		{
			@Override
			public boolean isMultiPart() {
				asked++;

				return multiPart;
			}
		};
		form.add(input);
		
		button1 = new AjaxFallbackButton("button1", null)
		{
			@Override
			protected void onSubmit(Optional<AjaxRequestTarget> target)
			{
				target.ifPresent(t -> t.add(this));
			}
		};
		form.add(button1);
		
		button2 = new AjaxFallbackButton("button2", null)
		{
			@Override
			protected void onSubmit(Optional<AjaxRequestTarget> target)
			{
				target.ifPresent(t -> t.add(this));
			}
		};
		form.add(button2);
	}
}