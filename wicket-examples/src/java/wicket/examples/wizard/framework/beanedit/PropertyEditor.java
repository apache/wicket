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
package wicket.examples.wizard.framework.beanedit;

import java.beans.PropertyDescriptor;

import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * Base class for custom property editors.
 *
 * @author Eelco Hillenius
 */
public abstract class PropertyEditor extends Panel
{
	/**
	 * Construct.
	 * @param id component id
	 * @param beanModel model with the target bean
	 * @param descriptor property descriptor
	 * @param editMode edit mode indicator
	 */
	public PropertyEditor(String id, IModel beanModel,
			PropertyDescriptor descriptor, EditMode editMode)
	{
		super(id);
	}

	/**
	 * Construct.
	 * @param id component id
	 * @param beanModel model with the target bean
	 * @param field the field
	 */
	public PropertyEditor(String id, IModel beanModel, Field field)
	{
		super(id);
	}
}
