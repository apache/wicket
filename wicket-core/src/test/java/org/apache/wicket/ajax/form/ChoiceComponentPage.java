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
package org.apache.wicket.ajax.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

/**
 * Page hosting a choice component.
 */
public class ChoiceComponentPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	public final Form<Void> form;

	public final TextField<String> text;

	public final RadioGroup<Boolean> group;

	public final Radio<Boolean> radioTrue;

	public final Radio<Boolean> radioFalse;

	/**
	 */
	public ChoiceComponentPage()
	{
		form = new Form<Void>("form");
		add(form);

		group = new RadioGroup<Boolean>("group", new Model(Boolean.TRUE));
		group.add(new AjaxFormChoiceComponentUpdatingBehavior()
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
			}
		});
		form.add(group);

		radioTrue = new Radio<Boolean>("radioTrue", Model.of(Boolean.TRUE));
		group.add(radioTrue);

		radioFalse = new Radio<Boolean>("radioFalse", Model.of(Boolean.FALSE));
		group.add(radioFalse);

		text = new TextField<String>("text", Model.of(""));
		group.add(text.setRequired(true));
	}
}
