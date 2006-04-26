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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Default property editor factory.
 * 
 * @author Eelco Hillenius
 */
public class PropertyEditorFactory implements IPropertyEditorFactory
{
	private static final long serialVersionUID = 1L;

	/** log. */
	private static Log log = LogFactory.getLog(PropertyEditorFactory.class);

	/** map of classes to property editor factories. */
	private Map classToFactory = new HashMap();

	/**
	 * default factory to be used when no registration was found for a certain
	 * class.
	 */
	private IPropertyEditorFactory defaultPropertyEditorFactory = new IPropertyEditorFactory()
	{
		private static final long serialVersionUID = 1L;

		public BeanPropertyEditor newPropertyEditor(String panelId, PropertyMeta propertyMeta,
				EditMode editMode)
		{
			return new BeanPanel.PropertyInput(panelId, propertyMeta);
		}
	};

	/**
	 * Construct.
	 */
	public PropertyEditorFactory()
	{
	}

	/**
	 * Sets a factory for with class c.
	 * 
	 * @param c
	 *            the type
	 * @param factory
	 *            the property editor factory
	 */
	public final void set(Class c, IPropertyEditorFactory factory)
	{
		if (factory == null)
		{
			throw new IllegalArgumentException("Argument factory may not be null");
		}
		if (c == null)
		{
			throw new IllegalArgumentException("Argument c (class) may not be null");
		}

		IPropertyEditorFactory replaced = (IPropertyEditorFactory)classToFactory.put(c, factory);

		if (replaced != null)
		{
			log.info("replaced property editor factory " + replaced + " with " + factory
					+ " for type " + c);
		}
		else
		{
			log.info("set property editor factory " + factory + " for type " + c);
		}
	}

	/**
	 * Gets the factory that is registered for class c.
	 * 
	 * @param c
	 *            the class to get the factory for
	 * @return the factory that is registered for class c or null if no type
	 *         converter was registered for class c
	 */
	public final IPropertyEditorFactory get(Class c)
	{
		return (IPropertyEditorFactory)classToFactory.get(c);
	}

	/**
	 * @see wicket.extensions.markup.html.beanedit.IPropertyEditorFactory#newPropertyEditor(java.lang.String,
	 *      PropertyMeta, wicket.extensions.markup.html.beanedit.EditMode)
	 */
	public BeanPropertyEditor newPropertyEditor(String panelId, PropertyMeta propertyMeta,
			EditMode editMode)
	{
		return null;
	}

}
