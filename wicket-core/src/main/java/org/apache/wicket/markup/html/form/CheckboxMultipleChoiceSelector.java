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

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;


/**
 * A Javascript-based "Select All" checkbox component that works with {@link CheckBoxMultipleChoice}
 * . By default, clicking on any of the controlled checkboxes automatically updates the state of the
 * "select all" checkbox. Override {@link AbstractCheckSelector#wantAutomaticUpdate()} to change
 * this.
 * 
 * @author Carl-Eric Menzel <cmenzel@wicketbuch.de>
 */
public class CheckboxMultipleChoiceSelector extends AbstractCheckSelector
{
	private static final long serialVersionUID = 1L;

	private final static ResourceReference JS = new PackageResourceReference(
		CheckboxMultipleChoiceSelector.class, "CheckboxMultipleChoiceSelector.js");

	private final CheckBoxMultipleChoice<?> choiceComponent;

	/**
	 * @param id
	 *            The component ID
	 * @param choiceComponent
	 *            The checkbox choice component this Selector will manage.
	 */
	public CheckboxMultipleChoiceSelector(String id, CheckBoxMultipleChoice<?> choiceComponent)
	{
		super(id);
		this.choiceComponent = choiceComponent;
		choiceComponent.setOutputMarkupId(true);
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.renderJavaScriptReference(JS);
	}

	@Override
	protected CharSequence getFindCheckboxesFunction()
	{
		return "Wicket.CheckboxSelector.Choice.findCheckboxesFunction('" +
			choiceComponent.getMarkupId() + "')";
	}
}
