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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;

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
public class DropDownChoice extends AbstractChoice implements IOnChangeListener
{
	/** serial UID. */
	private static final long serialVersionUID = 122777360064586107L;

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param values
	 *            The drop down values
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public DropDownChoice(String name, Serializable object, final Collection values)
	{
		super(name, object, values);
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param expression
	 *            See Component constructor
	 * @param values
	 *            The drop down values
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public DropDownChoice(String name, Serializable object, String expression,
			final Collection values)
	{
		super(name, object, expression, values);
	}

	/**
	 * @see FormComponent#getValue()
	 */
	public final String getValue()
	{
		final List list = getValues();
		if (list instanceof IDetachableChoiceList)
		{
			final int index = list.indexOf(getModelObject());
			if (index != -1)
			{
				return ((IDetachableChoiceList)list).getId(index);
			}
			return "-1";
		}
		else
		{
			return Integer.toString(list.indexOf(getModelObject()));
		}
	}

	/**
	 * Called when a selection changes.
	 */
	public final void onSelectionChanged()
	{
		onSelectionChanged(internalUpdateModel());
	}

	/**
	 * @see FormComponent#setValue(java.lang.String)
	 */
	public final void setValue(final String value)
	{
		final List list = getValues();
		if (list instanceof IDetachableChoiceList)
		{
			setModelObject(((IDetachableChoiceList)list).objectForId(value));
		}
		else
		{
			setModelObject(list.get(Integer.parseInt(value)));
		}
	}

	/**
	 * Updates this components' model from the request.
	 * 
	 * @see wicket.markup.html.form.AbstractChoice#updateModel()
	 */
	public final void updateModel()
	{
		internalUpdateModel();
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		// If a user subclasses this class and implements IOnChangeListener
		// an onChange scriptlet is added
		final String url = getRequestCycle().urlFor(this, IOnChangeListener.class);
		
		// NOTE: do not encode the url as that would give invalid JavaScript
		tag.put("onChange", "location.href='" + url + "&" + getPath()
				+ "=' + this.options[this.selectedIndex].value;");
		
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
	 *            The newly selected object of the backing model NOTE this is
	 *            the same as you would get by calling getModelObject() if the
	 *            new selection were current
	 */
	protected void onSelectionChanged(final Object newSelection)
	{
	}

	/**
	 * Update model and return the object.
	 * 
	 * @return the object
	 */
	private Object internalUpdateModel()
	{
		final String indexOrId = getInput();
		Object object = null;
		final List list = getValues();
		if (indexOrId == null || "".equals(indexOrId))
		{
			setModelObject(null);
		}
		else if (list instanceof IDetachableChoiceList)
		{
			object = ((IDetachableChoiceList)list).objectForId(indexOrId);
			setModelObject(object);
		}
		else
		{
			object = list.get(Integer.parseInt(indexOrId));
			setModelObject(object);
		}
		return object;
	}

	static
	{
		// Allow optional use of the IOnChangeListener interface
		RequestCycle.registerRequestListenerInterface(IOnChangeListener.class);
	}
}