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
package org.apache.wicket.examples.builder;

import java.util.List;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * The in-place editors of {@link org.apache.wicket.examples.ajax.builtin.EditableLabelPage},
 * built.
 */
public class EditableLabelPage extends BasePage
{
	private static final long serialVersionUID = 1L;

	private static final List<String> SITES = List.of("The Server Side", "Java Lobby",
		"Java.Net");

	public EditableLabelPage()
	{
		IModel<Integer> refreshes = Model.of(0);
		FeedbackPanel feedback = feedback("feedback").outputMarkupId().build();

		add(form("form").body(form -> form
			.add(feedback)
			.add(editableLabel("text1", Model.of("fox")),
				editableLabel("text2", Model.of("dog")),
				editableMultiLineLabel("text3", Model.of("multiple\nlines of\ntextual content")),
				editableChoiceLabel("site", Model.of(SITES.get(0)), SITES),
				editableLabel("number", Model.of(42))
					.converter(converter(Integer::parseInt, number -> String.format("%010d", number)))
					.apply(number -> number.setType(Integer.class)
						.setRequired(true)
						.setLabel(Model.of("Number")))
					.onSubmit(target -> target.add(feedback))
					.onError(target -> target.add(feedback)),
				label("refresh-counter", refreshes),
				link("refresh-link").onClick(() -> refreshes.setObject(refreshes.getObject() + 1)))));
	}
}
