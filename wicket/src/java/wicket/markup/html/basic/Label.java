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
package wicket.markup.html.basic;


import java.io.Serializable;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlComponent;
import wicket.model.IModel;

/**
 * A Label component must be attached to a SPAN tag, and replaces its body with the model
 * object.
 * <p>
 * Exactly what is displayed as the body, depends on the model. The simples case is a
 * static string, that can be constructed like:
 * 
 * <pre>
 * add(new Label(&quot;myLabel&quot;, &quot;the string to display&quot;))
 * </pre>
 * 
 * A dynamic body could for instance be created like: Using class:
 * 
 * <pre>
 * 
 * public class Person
 * {
 * 
 *     private String name;
 * 
 *     public String getName()
 *     {
 *         return name;
 *     }
 * 
 *     public void setName(String name)
 *     {
 *         this.name = name;
 *     }
 * 
 * }
 * </pre>
 * 
 * with label:
 * 
 * <pre>
 * 
 * 
 * 
 *    add(new Label(&quot;myLabel&quot;, person, &quot;name&quot;);
 * 
 * 
 *  
 * </pre>
 * 
 * will replace the span body with the 'name' property of the given person object.
 * </p>
 * @author Jonathan Locke
 */
public class Label extends HtmlComponent
{ // TODO finalize javadoc
    /** Serial Version ID */
	private static final long serialVersionUID = -2180588252471379004L;

	/**
     * Constructor that uses the provided {@link IModel} as its model. All components have
     * names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the model
     * @throws wicket.RenderException Thrown if the component has
     * been given a null name.
     */
    public Label(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel} as a dynamic model.
     * This model will be wrapped in an instance of {@link wicket.model.PropertyModel}
     * using the provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(myIModel, expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the instance of {@link IModel}from which the model object will be
     *            used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.RenderException Thrown if the component has
     * been given a null name.
     */
    public Label(String name, IModel model, String expression)
    {
        super(name, model, expression);
    }

    /**
     * Constructor that uses the provided object as a simple model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}. All components have
     * names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @throws wicket.RenderException Thrown if the component has
     * been given a null name.
     */
    public Label(String name, Serializable object)
    {
        super(name, object);
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object will be
     * wrapped in an instance of {@link wicket.model.Model} that will be wrapped
     * in an instance of {@link wicket.model.PropertyModel} using the provided
     * expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.RenderException Thrown if the component has been
     * given a null name.
     */
    public Label(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
     * Allows modification of component tag.
     * @param cycle The request cycle
     * @param tag The tag to modify
     * @see wicket.Component#handleComponentTag(RequestCycle,
     *      wicket.markup.ComponentTag)
     */
    protected final void handleComponentTag(final RequestCycle cycle, final ComponentTag tag)
    {
        checkTag(tag, "span");
        super.handleComponentTag(cycle, tag);
    }

    /**
     * @see wicket.Component#handleBody(wicket.RequestCycle,
     *      wicket.markup.MarkupStream,
     *      wicket.markup.ComponentTag)
     */
    protected void handleBody(final RequestCycle cycle, final MarkupStream markupStream,
            final ComponentTag openTag)
    {
        replaceBody(cycle, markupStream, openTag, getModelObjectAsString());
    }
}


