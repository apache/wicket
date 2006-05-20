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
import wicket.markup.html.form.FormComponent;

/**
 * A simple compound model which uses the component's name as the property
 * expression to retrieve properties on the nested model object.
 * 
 * @see wicket.model.IModel
 * @see wicket.model.Model
 * @see wicket.model.AbstractDetachableModel
 * 
 * @param <V>
 *            Type of model object this model holds
 * 
 * @author Jonathan Locke
 */
public class CompoundPropertyModel<V> extends AbstractPropertyModel<V> implements ICompoundModel<V>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            The model object, which may or may not implement IModel
	 */
	public CompoundPropertyModel(final V model)
	{
		super(model);
	}

	/**
	 * @see wicket.model.AbstractPropertyModel#propertyExpression(wicket.Component)
	 */
	@Override
	protected String propertyExpression(final Component component)
	{
		if (component == null)
		{
		    return null;
		}
		
		return component.getId();
	}

	/**
	 * @see wicket.model.AbstractPropertyModel#propertyType(wicket.Component)
	 */
	@Override
	protected Class propertyType(final Component component)
	{
		if (component instanceof FormComponent)
		{
			return ((FormComponent)component).getType();
		}
		return null;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return new StringBuffer(super.toString()).toString();
	}
}
