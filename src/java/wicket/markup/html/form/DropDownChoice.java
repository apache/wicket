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
import wicket.markup.ComponentTag;


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

    static
    {
        // allow optional use of the IOnChangeListener interface
        RequestCycle.registerListenerInterface(IOnChangeListener.class);
    }

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
     * @see wicket.Component#handleComponentTag(RequestCycle, wicket.markup.ComponentTag)
     */
    protected void handleComponentTag(final RequestCycle cycle, final ComponentTag tag)
    {
        if (this instanceof IOnChangeListener)
        {
            // if a user subclasses this class and implements IOnChangeListener
            // an onChange scriptlet is added
            String url = cycle.urlFor(this, IOnChangeListener.class);

            tag.put("onChange", "location.href='"
                    + url + "&" + getPath() + "=' + this.options[this.selectedIndex].value;");
        }

        super.handleComponentTag(cycle, tag);
    }

	/**
	 * @see wicket.markup.html.form.AbstractDropDownChoice#updateModel(wicket.RequestCycle)
	 */
	public final void updateModel(RequestCycle cycle)
	{
		internalUpdateModel(cycle);
	}

    /**
     * Update model and return the object.
     * @param cycle request object
     * @return the object
     */
    private Object internalUpdateModel(RequestCycle cycle)
    {
        final String indexOrId = getRequestString(cycle);
        Object object = null;
        final List list = getValues();
        if(list instanceof IIdList)
		{
			object = ((IIdList)list).getObjectById(indexOrId);
            setModelObject(object);
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
				object = list.get(index);
                setModelObject(object);
			}
		}
        return object;
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

    /**
     * called when a selection changed.
     * @param cycle the request cycle
     */
    public final void selectionChanged(RequestCycle cycle)
    {
        Object value = internalUpdateModel(cycle);
        selectionChanged(cycle, value);
    }

    /**
     * Template method that can be overriden by clients that implement
     * IOnChangeListener to be notified by onChange events of a select element.
     * This method does nothing by default.
     * 
     * @param cycle
     *           the request cycle
     * @param newSelection
     *           the newly selected object
     * @see wicket.markup.html.form.IOnChangeListener#selectionChanged(wicket.RequestCycle,java.lang.Object)
     */
    public void selectionChanged(RequestCycle cycle, Object newSelection)
    {
        // no nada
    }
}
