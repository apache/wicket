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
package wicket.markup.html.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import wicket.WicketRuntimeException;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.convert.ConversionException;

/**
 * Component used to connect instances of Check components into a group.
 * Instances of Check have to be in the component hierarchy somewhere below the
 * group component. The model of the CheckGroup component has to be an instance
 * of java.util.Collection. The model collection of the group is filled with
 * model objects of all selected Check components.
 * 
 * ie
 * 
 * <code>
 * <span wicket:id="checkboxgroup">
 *   ...
 *   <input type="radio" wicket:id="checkbox1">choice 1</input>
 *   ...
 *   <input type="radio" wicket:id="checkbox2">choice 2</input>
 *   ...
 * </span>
 * </code>
 * 
 * @see wicket.markup.html.form.Check
 * @see wicket.markup.html.form.CheckGroupSelector
 * 
 * <p>
 * Note: This component does not support cookie persistence
 * 
 * @author Igor Vaynberg (ivaynberg@users.sf.net)
 * 
 */
public class CheckGroup extends FormComponent implements IOnChangeListener
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor that will create a default model collection
	 * 
	 * @param id
	 *            component id
	 */
	public CheckGroup(String id)
	{
		super(id);
		setRenderBodyOnly(true);
	}

	/**
	 * Constructor that wraps the provided collection with the
	 * wicket.model.Model object
	 * 
	 * @param id
	 *            component id
	 * @param collection
	 *            collection to be used as the model
	 * 
	 */
	public CheckGroup(String id, Collection collection)
	{
		this(id, new Model((Serializable)collection));
	}

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public CheckGroup(String id, IModel model)
	{
		super(id, model);
		setRenderBodyOnly(true);
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#convertValue(String[])
	 */
	protected Object convertValue(String[] paths) throws ConversionException
	{
		List collection = new ArrayList();

		/*
		 * if the input is null we do not need to do anything since the model
		 * collection has already been cleared
		 */

		if (paths != null && paths.length > 0)
		{
			for (int i = 0; i < paths.length; i++)
			{
				String path = paths[i];

				if (path != null)
				{
					// retrieve the selected checkbox component
					Check checkbox = (Check)get(path);

					if (checkbox == null)
					{
						throw new WicketRuntimeException(
								"submitted http post value ["
										+ paths.toString()
										+ "] for CheckGroup component ["
										+ getPath()
										+ "] contains an illegal relative path "
										+ "element ["
										+ path
										+ "] which does not point to a Check component. Due to this the CheckGroup component cannot resolve the selected Check component pointed to by the illegal value. A possible reason is that componment hierarchy changed between rendering and form submission.");
					}

					// assign the value of the group's model
					collection.add(checkbox.getModelObject());
				}
			}
		}
		return collection;
	}

	/**
	 * @see FormComponent#updateModel()
	 */
	public void updateModel()
	{
		Collection collection = (Collection)getModelObject();
		if (collection == null)
		{
			collection = (Collection)getConvertedInput();
			setModelObject(collection);
		}
		else
		{
			modelChanging();
			collection.clear();
			collection.addAll((Collection)getConvertedInput());
			modelChanged();
			// call model.setObject()
			getModel().setObject(this, collection);
		}
	}

	/**
	 * Check group does not support persistence through cookies
	 * 
	 * @see wicket.markup.html.form.FormComponent#supportsPersistence()
	 */
	protected final boolean supportsPersistence()
	{
		return false;
	}

	/**
	 * Called when a selection changes.
	 */
	public final void onSelectionChanged()
	{
		convert();
		updateModel();
		onSelectionChanged((Collection)getModelObject());
	}

	/**
	 * Template method that can be overriden by clients that implement
	 * IOnChangeListener to be notified by onChange events of a select element.
	 * This method does nothing by default.
	 * <p>
	 * Called when a {@link Check} is clicked in a {@link CheckGroup} that wants
	 * to be notified of this event. This method is to be implemented by clients
	 * that want to be notified of selection events.
	 * 
	 * @param newSelection
	 *            The new selection of the {@link CheckGroup}. NOTE this is the
	 *            same as you would get by calling getModelObject() if the new
	 *            selection were current
	 */
	protected void onSelectionChanged(final Collection newSelection)
	{
	}

	/**
	 * This method should be overridden to return true if it is desirable to
	 * have on-selection-changed notifiaction.
	 * 
	 * @return true if component should receive on-selection-changed
	 *         notifications, false otherwise
	 */
	protected boolean wantOnSelectionChangedNotifications()
	{
		return false;
	}

}
