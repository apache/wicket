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
package wicket.markup.html.form;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import wicket.IModel;
import wicket.RequestCycle;


/**
 * A choice implemented as a dropdown menu/list. Framework users can extend this
 * class and optionally implement interface
 * {@link wicket.markup.html.form.IOnChangeListener}to implement
 * onChange behaviour of the HTML select element.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
public class DropDownChoice extends AbstractDropDownChoice
{
	private static final long serialVersionUID = 122777360064586107L;
	/**
	 * @param name
	 * @param model
	 * @param expression
	 * @param values
	 */
	public DropDownChoice(String name, IModel model, String expression, Collection values)
	{
		super(name, model, expression, values);

	}
	/**
	 * @param name
	 * @param object
	 * @param values
	 */
	public DropDownChoice(String name, Serializable object, Collection values)
	{
		super(name, object, values);

	}
	/**
	 * @param name
	 * @param object
	 * @param expression
	 * @param values
	 */
	public DropDownChoice(String name, Serializable object, String expression, Collection values)
	{
		super(name, object, expression, values);

	}
	/**
	 * @param name
	 * @param model
	 * @param values
	 */
	public DropDownChoice(String name, IModel model, Collection values)
	{
		super(name, model, values);
		
	}
	

	/**
	 * @see wicket.markup.html.form.AbstractDropDownChoice#updateModel(wicket.RequestCycle)
	 */
	public final void updateModel(RequestCycle cycle)
	{
		final String indexOrId = getRequestString(cycle);
		final List list = getValues();
		
		if(list instanceof IIdList)
		{
			setModelObject(((IIdList)list).getObjectById(indexOrId));
		}
		else
		{
			final int index = Integer.parseInt(indexOrId);
			if (index == NULL_VALUE)
			{
				setModelObject(null);
			}
			else
			{
				setModelObject(list.get(index));
			}
		}
	}

	/**
	 * @see wicket.markup.html.form.FormComponent.ICookieValue#getCookieValue()
	 */
	public final String getCookieValue()
	{
		final List list = getValues();
		if(list instanceof IIdList)
		{
			final int index = list.indexOf(getModelObject());
			if(index != -1)
			{
				return ((IIdList)list).getIdValue(index);
			}
			return "-1";
		}
		else
		{
			return Integer.toString(list.indexOf(getModelObject()));
		}
	}

	/**
	 * @see wicket.markup.html.form.FormComponent.ICookieValue#setCookieValue(java.lang.String)
	 */
	public final void setCookieValue(final String value)
	{
		final List list = getValues();
		if(list instanceof IIdList)
		{
			setModelObject(((IIdList)list).getObjectById(value));
		}
		else
		{
			setModelObject(list.get(Integer.parseInt(value)));
		}
	}
}
