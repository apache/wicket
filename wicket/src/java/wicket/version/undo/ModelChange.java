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
package wicket.version.undo;

import wicket.Component;
import wicket.model.IModel;
import wicket.util.lang.Objects;

/**
 * A model change operation.
 * 
 * @author Jonathan Locke
 */
class ModelChange extends Change
{
	private final Component component;
	private final IModel originalModel;
	
	ModelChange(final Component component)
	{
		this.component = component;
		originalModel = (IModel)Objects.clone(component.getModel()); 
	}

	void undo()
	{
		component.setModel(originalModel);
	}
}
