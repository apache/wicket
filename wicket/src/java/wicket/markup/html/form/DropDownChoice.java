/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.form;

import java.util.Collection;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.model.IChoiceList;
import wicket.model.IModel;

/**
 * A choice implemented as a dropdown menu/list. Framework users can extend this
 * class and optionally implement interface
 * {@link wicket.markup.html.form.IOnChangeListener}to implement onChange
 * behaviour of the HTML select element.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
public class DropDownChoice extends AbstractSingleSelectChoice implements IOnChangeListener
{
	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, Collection)
	 */
	public DropDownChoice(final String id, final Collection choices)
	{
		super(id, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IChoiceList)
	 */
	public DropDownChoice(final String id, final IChoiceList choices)
	{
		super(id, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, Collection)
	 */
	public DropDownChoice(final String id, IModel model, final Collection choices)
	{
		super(id, model, choices);
	}
	
	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, IChoiceList)
	 */
	public DropDownChoice(final String id, IModel model, final IChoiceList choices)
	{
		super(id, model, choices);
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
	 * Processes the component tag.
	 * 
	 * @param tag
	 *			  Tag to modify
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "select");
		
		// Should a roundtrip be made (have onSelectionChanged called) when the selection changed?
		if (wantOnSelectionChangedNotifications())
		{
			// url that points to this components IOnChangeListener method
			final String url = urlFor(IOnChangeListener.class);

			// NOTE: do not encode the url as that would give invalid JavaScript
			tag.put("onChange", "location.href='" + url + "&" + getPath()
					+ "=' + this.options[this.selectedIndex].value;");
		}

		super.onComponentTag(tag);
	}

	/**
	 * Template method that can be overriden by clients that implement
	 * IOnChangeListener to be notified by onChange events of a select element.
	 * This method does nothing by default.
	 * <p>
	 * Called when a option is selected of a dropdown list that wants to be
	 * notified of this event. This method is to be implemented by clients that
	 * want to be notified of selection events.
	 * 
	 * @param newSelection
	 *			  The newly selected object of the backing model NOTE this is
	 *			  the same as you would get by calling getModelObject() if the
	 *			  new selection were current
	 */
	protected void onSelectionChanged(final Object newSelection)
	{
	}

	/**
	 * @return True if this component's onSelectionChanged event handler should
	 *			called using javascript if the selection changes
	 */
	protected boolean wantOnSelectionChangedNotifications()
	{
		return false;
	}

	static
	{
		// Allow optional use of the IOnChangeListener interface
		RequestCycle.registerRequestListenerInterface(IOnChangeListener.class);
	}
}