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
package org.apache.wicket.examples.ajax.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

/**
 * Linked select boxes example
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ChoicePage extends BasePage
{
	private String selectedMake;

	private final Map<String, List<String>> modelsMap = new HashMap<>(); // map:company->model

	/**
	 * @return Currently selected make
	 */
	public String getSelectedMake()
	{
		return selectedMake;
	}

	/**
	 * @param selectedMake
	 *            The make that is currently selected
	 */
	public void setSelectedMake(String selectedMake)
	{
		this.selectedMake = selectedMake;
	}

	/**
	 * Constructor.
	 */
	public ChoicePage()
	{
		modelsMap.put("AUDI", Arrays.asList("A4", "A6", "TT"));
		modelsMap.put("CADILLAC", Arrays.asList("CTS", "DTS", "ESCALADE", "SRX", "DEVILLE"));
		modelsMap.put("FORD", Arrays.asList("CROWN", "ESCAPE", "EXPEDITION", "EXPLORER", "F-150"));

		IModel<List<String>> makeChoices = new AbstractReadOnlyModel<List<String>>()
		{
			@Override
			public List<String> getObject()
			{
				return new ArrayList<>(modelsMap.keySet());
			}

		};

		IModel<List<String>> modelChoices = new AbstractReadOnlyModel<List<String>>()
		{
			@Override
			public List<String> getObject()
			{
				List<String> models = modelsMap.get(selectedMake);
				if (models == null)
				{
					models = Collections.emptyList();
				}
				return models;
			}

		};

		Form<?> form = new Form("form");
		add(form);

		final DropDownChoice<String> makes = new DropDownChoice<>("makes",
			new PropertyModel<String>(this, "selectedMake"), makeChoices);

		final DropDownChoice<String> models = new DropDownChoice<>("models",
			new Model<String>(), modelChoices);
		models.setOutputMarkupId(true);

		form.add(makes);
		form.add(models);

		makes.add(new AjaxFormComponentUpdatingBehavior("change")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				target.add(models);
			}
		});
	}
}