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
package wicket.extensions.markup.html.beanedit;

import java.io.Serializable;

/**
 * Factory for property editors.
 * 
 * @author Eelco Hillenius
 */
public interface IPropertyEditorFactory extends Serializable
{
	/**
	 * Creates a new property editor.
	 * 
	 * @param panelId
	 *            id of the panel; must be used for constructing any panel
	 * @param propertyMeta
	 *            property descriptor
	 * @param editMode
	 *            edit mode
	 * @return a property editor
	 */
	BeanPropertyEditor newPropertyEditor(String panelId, PropertyMeta propertyMeta,
			EditMode editMode);
}
