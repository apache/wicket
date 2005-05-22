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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wicket.model.IModel;

/**
 * Typesafe collection of Field objects.
 *
 * @author Eelco Hillenius
 */
public class Fields implements Serializable
{
	/** the target model; e.g. the model that holds the bean. */
	private final IModel targetModel;

	/** display name. */
	private String displayName;

	/** the fields. */
	private List fields = new ArrayList();

	/** fields stored on name for quick access. */
	private Map fieldOnName = new HashMap();

	/**
	 * Construct.
	 * @param targetModel the target model; e.g. the model that holds the bean
	 */
	public Fields(IModel targetModel)
	{
		this.targetModel = targetModel;
	}

	/**
	 * Adds a field.
	 * @param field the field to add
	 * @return This
	 */
	public final Fields add(Field field)
	{
		fields.add(field);
		fieldOnName.put(field.getName(), field);
		return this;
	}

	/**
	 * Removes a field.
	 * @param field the field to remove
	 */
	public final void remove(Field field)
	{
		fields.remove(field);
		fieldOnName.remove(field.getName());
	}

	/**
	 * Removes a field.
	 * @param name name of the field
	 */
	public final void remove(String name)
	{
		Field field = get(name);
		remove(field);
	}

	/**
	 * Gets a field using its name.
	 * @param name name of the field
	 * @return the field or null if not found
	 */
	public final Field get(String name)
	{
		return (Field)fieldOnName.get(name);
	}

	/**
	 * Gets the fields.
	 * @return the fields
	 */
	public final List list()
	{
		return fields;
	}

	/**
	 * Gets the targetModel, e.g. the model that references the bean.
	 * @return targetModel
	 */
	public final IModel getTargetModel()
	{
		return targetModel;
	}

	/**
	 * Gets the displayName.
	 * @return displayName
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * Sets the displayName.
	 * @param displayName displayName
	 */
	public final void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
}
