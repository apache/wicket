/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.customcomponents;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.IOnChangeListener;
import wicket.markup.html.form.TextField;
import wicket.model.IModel;

/**
 * Custom text field that listents to onchange events.
 *
 * @author Eelco Hillenius
 */
public class OnChangeTextField extends TextField implements IOnChangeListener
{
	/**
	 * Construct.
	 * @param id component id
	 */
	public OnChangeTextField(String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 * @param id component id
	 * @param type type
	 */
	public OnChangeTextField(String id, Class type)
	{
		super(id, type);
	}

	/**
	 * Construct.
	 * @param id component id
	 * @param model model
	 */
	public OnChangeTextField(String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * Construct.
	 * @param id component id
	 * @param model model
	 * @param type type
	 */
	public OnChangeTextField(String id, IModel model, Class type)
	{
		super(id, model, type);
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag Tag to modify
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		// url that points to this components IOnChangeListener method
		final String url = urlFor(IOnChangeListener.class);

		// NOTE: do not encode the url as that would give invalid JavaScript
		tag.put("onChange", "location.href='" + url + "&" + getPath()
				+ "=' + this.value;");

		super.onComponentTag(tag);
	}

	/**
	 * Called when a selection changes.
	 */
	public final void onSelectionChanged()
	{
		updateModel();
		onSelectionChanged(getModelObject());
	}

	/**
	 * Template method that can be overriden by clients that implement
	 * IOnChangeListener to be notified by onChange events of a select element.
	 * This method does nothing by default.
	 * <p>
	 * 
	 * @param newSelection
	 *			  The newly selected object of the backing model NOTE this is
	 *			  the same as you would get by calling getModelObject()
	 */
	protected void onSelectionChanged(final Object newSelection)
	{
	}

	static
	{
		// Allow optional use of the IOnChangeListener interface
		RequestCycle.registerRequestListenerInterface(IOnChangeListener.class);
	}
}
