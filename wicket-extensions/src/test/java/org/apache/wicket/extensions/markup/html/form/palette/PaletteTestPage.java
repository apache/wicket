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
package org.apache.wicket.extensions.markup.html.form.palette;

import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

/**
 * tests palette
 */
public class PaletteTestPage extends WebPage
{

	private static final long serialVersionUID = 1L;

	public Form<Object> form;

	public Palette<String> palette;

	/**
	 * Constructor
	 */
	public PaletteTestPage(IModel<List<String>> selected, IModel<List<String>> all)
	{
		form = new Form<>("form", new CompoundPropertyModel<Object>("WICKET-5086"));
		add(form);

		IChoiceRenderer<String> choiceRenderer = new ChoiceRenderer<String>()
		{
			@Override
			public Object getDisplayValue(String s)
			{
				return s;
			}

			@Override
			public String getIdValue(String s, int index)
			{
				return s;
			}
		};


		palette = new Palette<>("palette", selected, all, choiceRenderer, 10, true);
		form.add(palette);
	}
}
