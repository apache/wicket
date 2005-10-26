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
package wicket.extensions.markup.html.repeater.data.table;

import wicket.Component;
import wicket.model.IModel;

/**
 * model wrapper that returns the specified default value if the model object is
 * null
 */
public class LenientModelWrapper implements IModel
{
	private static final long serialVersionUID = 1L;

	private IModel model;
	private Object defaultValue;

	/**
	 * @param model
	 *            model to be wrapped
	 * @param defaultValue
	 *            default value to be returned if model object is null
	 */
	public LenientModelWrapper(IModel model, Object defaultValue)
	{
		this.model = model;
		this.defaultValue = defaultValue;
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return model.getNestedModel();
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		try
		{
			return model.getObject(component);
		}
		catch (RuntimeException e)
		{
			return defaultValue;
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}

	/**
	 * @see wicket.model.IModel#setObject(wicket.Component, Object)
	 */
	public void setObject(Component component, Object object)
	{
		model.setObject(component, object);
	}

	/**
	 * @see wicket.model.IModel#detach()
	 */
	public void detach()
	{
		model.detach();
	}

}
