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
package wicket.model;

import wicket.Component;

/**
 * A compound property model is a model which
 * 
 * @see wicket.model.IModel
 * @see wicket.model.Model
 * @see wicket.model.AbstractDetachableModel
 * 
 * @author Jonathan Locke
 */
public class CompoundPropertyModel extends AbstractPropertyModel
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -3136339624173288385L;

	/** Any nested model object */
	private final IModel model;

	/** The model object */
	private final Object modelObject;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            Nested model
	 */
	public CompoundPropertyModel(final IModel model)
	{
		if (model == null)
		{
			throw new IllegalArgumentException("Model parameter must not be null");
		}

		this.model = model;
		this.modelObject = null;
	}

	/**
	 * Constructor
	 * 
	 * @param modelObject
	 *            The model object
	 */
	public CompoundPropertyModel(final Object modelObject)
	{
		if (modelObject == null)
		{
			throw new IllegalArgumentException("Model parameter must not be null");
		}
		
		this.modelObject = modelObject;
		this.model = null;
	}

	/**
	 * @return The nested model object
	 */
	public final IModel getNestedModel()
	{
		return model;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[CompoundPropertyModel model = " + model + ", modelObject = " + modelObject + "]";
	}

	/**
	 * @see wicket.model.AbstractPropertyModel#modelObject(Component)
	 */
	protected Object modelObject(final Component component)
	{
		return model != null ? model.getObject(component) : modelObject;
	}

	/**
	 * @see wicket.model.AbstractPropertyModel#ognlExpression(wicket.Component)
	 */
	protected String ognlExpression(final Component component)
	{
		return component.getName();
	}

	/**
	 * @see wicket.model.AbstractDetachableModel#onAttach()
	 */
	protected void onAttach()
	{
	}

	/**
	 * @see AbstractDetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		super.onDetach();
		model.detach();
	}

	/**
	 * @see wicket.model.AbstractPropertyModel#propertyType(wicket.Component)
	 */
	protected Class propertyType(final Component component)
	{
		return null;
	}
}
