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
package org.apache.wicket.examples;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.theme.Theme;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.model.IModel;

/**
 * A drop-down choosing the {@link Theme} of the extensions components of an example, applied via
 * Ajax by {@link #onThemeChanged(AjaxRequestTarget)}.
 */
public abstract class ThemeChoice extends DropDownChoice<Theme>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 *            component id, attached to a {@code <select>}
	 * @param theme
	 *            the chosen theme
	 */
	public ThemeChoice(String id, IModel<Theme> theme)
	{
		super(id, theme, List.of(Theme.values()));
		setChoiceRenderer(new EnumChoiceRenderer<>(this));
		setRequired(true);
		add(new AjaxFormComponentUpdatingBehavior("change")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onThemeChanged(target);
			}
		});
	}

	/**
	 * Called after the user chose another theme; re-render the themed components here.
	 *
	 * @param target
	 *            the Ajax request target
	 */
	protected abstract void onThemeChanged(AjaxRequestTarget target);
}
