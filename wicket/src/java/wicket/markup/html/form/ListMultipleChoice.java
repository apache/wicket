/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * A multiple choice list component.
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 */
public final class ListMultipleChoice extends AbstractChoice
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -1000324612688307682L;

	/**
	 * Constructor.
	 * 
	 * @param componentName
	 *            the name of the component
	 * @param model
	 *            the component model
	 * @param values
	 *            the values to choose from
	 */
	public ListMultipleChoice(final String componentName, final IModel model, final List values)
	{
		super(componentName, model, values);
		setRenderNullOption(false);
	}

	/**
	 * Convenience constructor; wraps the given model in a {@link Model}object.
	 * 
	 * @param componentName
	 *            the name of the component
	 * @param model
	 *            the component model; will be wraped in a {@link Model}object
	 * @param values
	 *            the values to choose from
	 */
	public ListMultipleChoice(final String componentName, final Serializable model,
			final List values)
	{
		this(componentName, new Model(model), values);
	}

	/**
	 * @see FormComponent#getValue()
	 */
	public final String getValue()
	{
		// Get the list of selected values
		final Collection selectedValues = (Collection)getModelObject();
		final StringBuffer cookieValue = new StringBuffer();
		if (selectedValues != null)
		{
			final List list = getValues();
			final Iterator it = selectedValues.iterator();
			while (it.hasNext())
			{
				final int index = list.indexOf(it.next());
				if (list instanceof IIdList)
				{
					cookieValue.append(((IIdList)list).getIdValue(index));
				}
				else
				{
					cookieValue.append(index);
				}
				// the id's can't have ; in there id!! should we escape it or
				// something??
				cookieValue.append(";");
			}
		}
		return cookieValue.toString();
	}

	/**
	 * Sets the cookie value for this component.
	 * 
	 * @param value
	 *            the cookie value for this component
	 * @see FormComponent#setValue(java.lang.String)
	 */
	public final void setValue(final String value)
	{
		Collection selectedValues = (Collection)getModelObject();
		if (selectedValues == null)
		{
			selectedValues = new ArrayList();
			setModelObject(selectedValues);
		}
		else
		{
			selectedValues.clear();
		}
		final List list = getValues();
		final StringTokenizer st = new StringTokenizer(value, ";");
		while (st.hasMoreTokens())
		{
			final String idOrIndex = st.nextToken();
			if (list instanceof IIdList)
			{
				selectedValues.add(((IIdList)list).getObjectById(idOrIndex));
			}
			else
			{
				final int index = Integer.parseInt(idOrIndex);
				selectedValues.add(list.get(index));
			}
		}
	}

	/**
	 * Updates this forms model from the request.
	 * 
	 * @see FormComponent#updateModel()
	 */
	public final void updateModel()
	{
		// Get the list of selected values
		Collection selectedValues = (Collection)getModelObject();

		if (selectedValues != null)
		{
			selectedValues.clear();
		}
		else
		{
			selectedValues = new ArrayList();
			setModelObject(selectedValues);
		}

		// Get indices selected from request
		final String[] indicesOrIds = getRequestStrings();

		if (indicesOrIds != null)
		{
			final List list = getValues();

			// Loop through selected indices
			for (int i = 0; i < indicesOrIds.length; i++)
			{
				if (list instanceof IIdList)
				{
					selectedValues.add(((IIdList)list).getObjectById(indicesOrIds[i]));
				}
				else
				{
					final int index = Integer.parseInt(indicesOrIds[i]);
					// Add the value at the given index to the collection of
					// selected values
					selectedValues.add(list.get(index));
				}
			}
		}
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#handleComponentTag(ComponentTag)
	 */
	protected void handleComponentTag(final ComponentTag tag)
	{
		super.handleComponentTag(tag);
		tag.put("multiple", true);
	}

	/**
	 * Gets whether the given value represents the current selection.
	 * 
	 * @param currentValue
	 *            the current list value
	 * @return whether the given value represents the current selection
	 * @see wicket.markup.html.form.AbstractChoice#isSelected(java.lang.Object)
	 */
	protected boolean isSelected(Object currentValue)
	{
		Collection collection = (Collection)getModelObject();
		if (collection != null)
			return collection.contains(currentValue);
		return false;
	}
}