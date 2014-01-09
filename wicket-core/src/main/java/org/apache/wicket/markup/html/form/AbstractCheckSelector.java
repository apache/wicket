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

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.ajax.WicketEventJQueryResourceReference;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Base class for all Javascript-based "select-all" checkboxes. Provides a simple "select all"
 * checkbox which can be automatically updated based on the selection state of the checkboxes it
 * controls (see {@link #wantAutomaticUpdate()}).
 * 
 * @see CheckboxMultipleChoiceSelector
 * @see CheckGroupSelector
 * @see CheckBoxSelector
 * 
 * @author Carl-Eric Menzel <cmenzel@wicketbuch.de>
 */
public abstract class AbstractCheckSelector extends LabeledWebMarkupContainer
	implements
		IHeaderContributor
{
	private static final long serialVersionUID = 1L;

	private static final ResourceReference JS = new JavaScriptResourceReference(
		AbstractCheckSelector.class, "CheckSelector.js")
	{

		/**
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public List<HeaderItem> getDependencies()
		{
			List<HeaderItem> dependencies = super.getDependencies();
			ResourceReference wicketEventReference = WicketEventJQueryResourceReference.get();
			if (Application.exists()) {
				wicketEventReference = Application.get().getJavaScriptLibrarySettings().getWicketEventReference();
			}
			dependencies.add(JavaScriptHeaderItem.forReference(wicketEventReference));
			return dependencies;
		}
	};

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 */
	public AbstractCheckSelector(String id)
	{
		super(id);
		setOutputMarkupId(true);
	}

	/**
	 * @return Whether the individual checkboxes should update the state of the Selector. If true,
	 *         then when a checkbox is clicked, the state of all checkboxes is tested - if all are
	 *         checked, the selector is checked too. If not, the selector is unchecked.
	 */
	protected boolean wantAutomaticUpdate()
	{
		return true;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(JS));

		String findCheckboxes = getFindCheckboxesFunction().toString();

		// initialize the selector
		response.render(OnLoadHeaderItem.forScript("Wicket.CheckboxSelector.initializeSelector('" +
			this.getMarkupId() + "', " + findCheckboxes + ");"));
		if (wantAutomaticUpdate())
		{
			// initialize the handlers for automatic updating of the selector state
			response.render(OnLoadHeaderItem.forScript("Wicket.CheckboxSelector.attachUpdateHandlers('" +
				this.getMarkupId() + "', " + findCheckboxes + ");"));
		}
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		if (isEnableAllowed() && isEnabledInHierarchy())
		{
			tag.remove("disabled");
		}
		else
		{
			tag.put("disabled", "disabled");
		}
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "checkbox");
	}

	/**
	 * Concrete subclasses must override this to provide a Javascript function that returns the IDs
	 * of all checkboxes that should be controlled by this selector.
	 * 
	 * @return a String containing a Javascript expression that evaluates to a function(!). This
	 *         function must return an array containing the IDs of all checkbox input elements that
	 *         this selector should control.
	 */
	protected abstract CharSequence getFindCheckboxesFunction();
}
