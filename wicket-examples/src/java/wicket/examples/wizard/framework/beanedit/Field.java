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


/**
 * Field for user input.
 *
 * @author Eelco Hillenius
 */
public class Field extends AbstractField
{
	/** type of the field. */
	private final Class type;

	/** The edit mode. */
	private EditMode editMode = EditMode.READ_WRITE;

	/**
	 * Construct.
	 * @param name logical name of the field
	 * @param displayName name to display
	 * @param type the type of the field
	 */
	public Field(String name, String displayName, Class type)
	{
		super(name, displayName);
		this.type = type;
	}

	/**
	 * Gets the type of the field.
	 * @return the type of the field
	 */
	public Class getType()
	{
		return type;
	}

	/**
	 * Gets the edit mode of this field.
	 * @return the edit mode
	 */
	public EditMode getEditMode()
	{
		return editMode;
	}

	/**
	 * Sets the edit mode.
	 * @param editMode the edit mode
	 * @return This
	 */
	public Field setEditMode(EditMode editMode)
	{
		this.editMode = editMode;
		return this;
	}
}
