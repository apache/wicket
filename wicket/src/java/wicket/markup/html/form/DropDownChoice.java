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

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.model.IModel;


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
{ // TODO finalize javadoc
	private static final long serialVersionUID = 122777360064586107L;

    static
    {
        // allow optional use of the IOnChangeListener interface
        RequestCycle.registerRequestListenerInterface(IOnChangeListener.class);
    }

    /**
     * Constructor that uses the provided {@link IModel}as its model. All components have
     * names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the model
     * @param values the drop down values
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public DropDownChoice(String name, IModel model, final Collection values)
    {
        super(name, model, values);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel}as a dynamic model.
     * This model will be wrapped in an instance of {@link wicket.model.PropertyModel}using the
     * provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(myIModel, expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the instance of {@link IModel}from which the model object will be
     *            used as the subject for the given expression
     * @param values the drop down values
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public DropDownChoice(String name, IModel model, String expression,
            final Collection values)
    {
        super(name, model, expression, values);
    }

    /**
     * Constructor that uses the provided object as a simple model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}. All components have names. A
     * component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @param values the drop down values
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public DropDownChoice(String name, Serializable object, final Collection values)
    {
        super(name, object, values);
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}that will be wrapped in an instance
     * of {@link wicket.model.PropertyModel}using the provided expression. Thus, using this
     * constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @param values the drop down values
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public DropDownChoice(String name, Serializable object, String expression,
            final Collection values)
    {
        super(name, object, expression, values);
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
            url = url.replaceAll("&", "&amp;");
            tag.put("onChange", "location.href='"
                    + url + "&amp;" + getPath() +
                    "=' + this.options[this.selectedIndex].value;");
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
