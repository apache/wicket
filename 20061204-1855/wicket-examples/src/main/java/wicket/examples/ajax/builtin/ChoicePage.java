/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.ajax.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wicket.Component;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * Linked select boxes example
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ChoicePage extends BasePage
{
	private String selectedMake;

	private Map modelsMap = new HashMap(); // map:company->model

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
		modelsMap.put("AUDI", Arrays.asList(new String[] { "A4", "A6", "TT" }));
		modelsMap.put("CADILLAC", Arrays.asList(new String[] { "CTS", "DTS", "ESCALADE", "SRX",
				"DEVILLE" }));
		modelsMap.put("FORD", Arrays.asList(new String[] { "CROWN", "ESCAPE", "EXPEDITION",
				"EXPLORER", "F-150" }));

		IModel makeChoices = new AbstractReadOnlyModel()
		{
			public Object getObject(Component component)
			{
				Set keys = modelsMap.keySet();
				List list = new ArrayList(keys);
				return list;
			}

		};

		IModel modelChoices = new AbstractReadOnlyModel()
		{
			public Object getObject(Component component)
			{
				List models = (List)modelsMap.get(selectedMake);
				if (models == null)
				{
					models = Collections.EMPTY_LIST;
				}
				return models;
			}

		};

		Form form = new Form("form");
		add(form);

		final DropDownChoice makes = new DropDownChoice("makes", new PropertyModel(this,
				"selectedMake"), makeChoices);

		final DropDownChoice models = new DropDownChoice("models", new Model(), modelChoices);
		models.setOutputMarkupId(true);

		form.add(makes);
		form.add(models);

		makes.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{
			protected void onUpdate(AjaxRequestTarget target)
			{
				target.addComponent(models);
			}
		});
	}
}