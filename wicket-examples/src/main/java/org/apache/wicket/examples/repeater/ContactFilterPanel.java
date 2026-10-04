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
package org.apache.wicket.examples.repeater;

import java.time.Duration;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * A text field filtering contacts while the user types, shown in front of the navigation of the
 * DynamicDataTable example.
 */
abstract class ContactFilterPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	ContactFilterPanel(String id, IModel<String> filter)
	{
		super(id);

		TextField<String> field = new TextField<>("filter", filter);
		field.add(new AjaxFormComponentUpdatingBehavior("input")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				attributes.setThrottlingSettings(
					new ThrottlingSettings(Duration.ofMillis(300), true));
			}

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onFilter(target);
			}
		});
		add(field);
	}

	/**
	 * Called after the filter changed.
	 */
	protected abstract void onFilter(AjaxRequestTarget target);
}
