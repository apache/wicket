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
package wicket.extensions.markup.html.beanedit;

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
public class BeanFields implements Serializable
{
	/** the target model; e.g. the model that holds the bean. */
	private final BeanModel beanModel;

	/** display name. */
	private String displayName;

	/** the fields. */
	private List fields = new ArrayList();

	/** fields stored on name for quick access. */
	private Map fieldOnName = new HashMap();

	/**
	 * Construct.
	 * @param beanModel the target model; e.g. the model that holds the bean
	 */
	public BeanFields(BeanModel beanModel)
	{
		this.beanModel = beanModel;
	}

	/**
	 * Adds a field.
	 * @param field the field to add
	 * @return This
	 */
	public final BeanFields add(BeanField field)
	{
		fields.add(field);
		fieldOnName.put(field.getName(), field);
		return this;
	}

	/**
	 * Removes a field.
	 * @param field the field to remove
	 */
	public final void remove(BeanField field)
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
		BeanField field = get(name);
		remove(field);
	}

	/**
	 * Gets a field using its name.
	 * @param name name of the field
	 * @return the field or null if not found
	 */
	public final BeanField get(String name)
	{
		return (BeanField)fieldOnName.get(name);
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
	public final IModel getBeanModel()
	{
		return beanModel;
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
