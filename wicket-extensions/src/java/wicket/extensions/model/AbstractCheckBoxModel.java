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
package wicket.extensions.model;

import wicket.model.IModel;

/**
 * Model adapter that makes working with models for checkboxes easier.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractCheckBoxModel implements IModel<Boolean>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @return true to indicate the checkbox should be selected, false otherwise
	 */
	public abstract boolean isSelected();

	/**
	 * Called when checkbox has been selected
	 * 
	 */
	public abstract void select();

	/**
	 * Called when checkbox is unselected
	 * 
	 */
	public abstract void unselect();

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return null;
	}

	/**
	 * 
	 * @see wicket.model.IModel#getObject()
	 */
	public final Boolean getObject()
	{
		return Boolean.valueOf(isSelected());
	}

	/**
	 * @see wicket.model.IModel#setObject(java.lang.Object)
	 */
	public final void setObject(Boolean object)
	{
		if (Boolean.TRUE.equals(object))
		{
			select();
		}
		else
		{
			unselect();
		}
	}

}
