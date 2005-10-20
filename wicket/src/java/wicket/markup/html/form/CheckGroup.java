/*
 * $Id$ $Revision$ $Date$
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

import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Component used to connect instances of Check components into a
 * group. Instances of Check have to be in the component hierarchy
 * somewhere below the group component. The model of the CheckGroup component
 * has to be an instance of java.util.Collection. If no model is provided a new
 * instance of java.util.ArrayList will be used as the default model collection.
 * The model collection of the group is filled with model objects of all
 * selected Check components.
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
 * @author Igor Vaynberg (ivaynberg@users.sf.net)
 * 
 */
public class CheckGroup extends FormComponent
{
	/**
	 * 
	 */
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
	 * @see FormComponent#updateModel()
	 */
	public void updateModel()
	{
		modelChanging();

		Collection collection = (Collection)getModelObject();
		collection.clear();

		/*
		 * the input contains an array of full path of the checcked checkbox
		 * components unless nothing was selected in which case the input
		 * contains null
		 */
		String[] paths = inputAsStringArray();

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
					/*
					 * checkbox component path sans group path = relative path
					 * from group to checkbox since we know the checkbox is
					 * child of group
					 */

					path = path.substring(getPath().length() + 1);

					// retrieve the selected checkbox component
					Check checkbox = (Check)get(path);

					// assign the value of the group's model
					collection.add(checkbox.getModelObject());
				}
			}
		}

		modelChanged();
	}

}
