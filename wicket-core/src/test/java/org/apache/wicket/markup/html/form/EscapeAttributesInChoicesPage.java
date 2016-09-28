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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EscapeAttributesInChoicesPage extends WebPage {
	private static final long serialVersionUID = 1L;

	public EscapeAttributesInChoicesPage(final PageParameters parameters) {
		super(parameters);

		final Map<String, String> fruits = new HashMap<>();
		fruits.put("apple\" onmouseover=\"alert('hi');\" \"", "Apple");

		IChoiceRenderer<String> iChoiceRenderer = new ChoiceRenderer<String>() {
			@Override
			public Object getDisplayValue(final String s) {
				return fruits.get(s);
			}

			@Override
			public String getIdValue(final String s, final int i) {
				return s;
			}
		};

		add(new RadioChoice<>("radiofield", new ArrayList<>(fruits.keySet()), iChoiceRenderer));
		add(new DropDownChoice<>("dropdownfield", new ArrayList<>(fruits.keySet()), iChoiceRenderer));
		add(new CheckBoxMultipleChoice<>("checkboxfield", new ArrayList<>(fruits.keySet()), iChoiceRenderer));


    }
}
