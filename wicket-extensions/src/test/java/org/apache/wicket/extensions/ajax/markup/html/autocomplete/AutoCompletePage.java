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
package org.apache.wicket.extensions.ajax.markup.html.autocomplete;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

/**
 * @author svenmeier
 */
public class AutoCompletePage extends WebPage
{
	private static final long serialVersionUID = 1L;
	private AutoCompleteTextField<String> textField;

	/**
	 * Construct.
	 */
	public AutoCompletePage()
	{
		textField = new AutoCompleteTextField<String>("textfield", Model.of(""))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator<String> getChoices(String input)
			{
				return Arrays.<String> asList().iterator();
			}
		};
		textField.add(new AjaxEventBehavior("change")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
			}
		});
		add(textField);

		add(new AjaxLink<Void>("link")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				target.add(textField);
			}
		});
	}
}
