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

import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.model.IModel;

/**
 * HTML checkbox input component.
 *
 * @author Jonathan Locke
 */
public final class CheckBox extends FormComponent
{
    /** Serial Version ID. */
	private static final long serialVersionUID = 7559827519977114184L;

	/**
     * Constructor that uses the provided {@link IModel} as its model. All components have
     * names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the model
     * @throws wicket.WicketRuntimeException Thrown if the component has been given a null name.
     */
    public CheckBox(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel} as a dynamic model.
     * This model will be wrapped in an instance of {@link wicket.model.PropertyModel}using the
     * provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(myIModel, expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the instance of {@link IModel} from which the model object will be
     *            used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.WicketRuntimeException Thrown if the component has been given a null name.
     */
    public CheckBox(String name, IModel model, String expression)
    {
        super(name, model, expression);
    }

    /**
     * Constructor that uses the provided object as a simple model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}. All components have names. A component's
     * name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @throws wicket.WicketRuntimeException Thrown if the component has been given a null name.
     */
    public CheckBox(String name, Serializable object)
    {
        super(name, object);
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object will be
     * wrapped in an instance of {@link wicket.model.Model} that will be wrapped in an instance of
     * {@link wicket.model.PropertyModel} using the provided expression. Thus, using this constructor
     * is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.WicketRuntimeException Thrown if the component has been given a null name.
     */
    public CheckBox(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
     * @see FormComponent#supportsPersistence()
     */
    public boolean supportsPersistence()
    {
        return true;
    }

    /**
     * @see FormComponent#setValue(java.lang.String)
     */
    public void setValue(String value)
    {
        setModelObject(Boolean.valueOf(value));
    }

    /**
     * Updates this components' model from the request.
     * @see wicket.markup.html.form.FormComponent#updateModel()
     */
    public void updateModel()
    {
        setModelObject(getRequestBoolean());
    }

    /**
	 * Processes the component tag.
	 * @param tag Tag to modify
     * @see wicket.Component#handleComponentTag(ComponentTag)
     */
    protected void handleComponentTag(final ComponentTag tag)
    {
        checkTag(tag, "input");
        checkAttribute(tag, "type", "checkbox");
        super.handleComponentTag(tag);

        Object value = getModelObject();

        if (value != null)
        {
            if (!(value instanceof Boolean))
            {
                throw new WicketRuntimeException(
                        "Model objects for checkboxes have to be of type Boolean");
            }

            if (((Boolean)value).booleanValue())
            {
                tag.put("checked", "true");
            }
        }
    }
}


