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
package org.apache.wicket.extensions.wizard;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * A bar of buttons for wizards utilizing {@link AjaxFormSubmitBehavior}.
 * 
 * @see Wizard#newButtonBar(String)
 * 
 * @author svenmeier
 */
public class AjaxWizardButtonBar extends WizardButtonBar
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param wizard
	 *            The containing wizard
	 */
	public AjaxWizardButtonBar(String id, Wizard wizard)
	{
		super(id, wizard);

		wizard.setOutputMarkupId(true);
	}

	@Override
	public MarkupContainer add(Component... childs)
	{
		for (Component component : childs)
		{
			if (component instanceof WizardButton)
			{
				ajaxify((WizardButton)component);
			}
		}
		return super.add(childs);
	}

	private void ajaxify(final WizardButton button)
	{
		button.add(new AjaxFormSubmitBehavior("click")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);

				AjaxWizardButtonBar.this.updateAjaxAttributes(attributes);
			}

			@Override
			public boolean getDefaultProcessing()
			{
				return button.getDefaultFormProcessing();
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				target.add(findParent(Wizard.class));

				button.onSubmit();
			}

			@Override
			protected void onAfterSubmit(AjaxRequestTarget target)
			{
				button.onAfterSubmit();
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				target.add(findParent(Wizard.class));

				button.onError();
			}
			
			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				// WICKET-5644 prevent non-Ajax submit (similar to AjaxButton WICKET-5594)
				tag.put("type", "button");
			}
		});
	}

	/**
	 * Hook method to update Ajax attributes.
	 * 
	 * @param attributes
	 *            Ajax attributes
	 */
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}
}