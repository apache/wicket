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
package wicket.markup.html.link;

import java.io.Serializable;

import wicket.RequestCycle;
import wicket.markup.html.HtmlContainer;
import wicket.model.IModel;

/**
 * Base class for links.
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public abstract class AbstractLink extends HtmlContainer implements ILinkListener
{
    static
    {
        // Allow calls through the ILinkListener interface
        RequestCycle.registerRequestListenerInterface(ILinkListener.class);
    }

    /**
     * The popup specification. If not-null, a javascript on-click event handler will be
     * generated that opens a new window using the popup properties.
     */
    private PopupSpecification popupSpecification = null;

    /**
     * Construct.
     * @param componentName the name of the component
     */
    public AbstractLink(String componentName)
    {
        super(componentName);
    }

    /**
     * Constructor that uses the provided {@link IModel} as its model.
     * @param name The non-null name of this component
     * @param model the model
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public AbstractLink(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel} as a dynamic model.
     * This model will be wrapped in an instance of {@link wicket.model.PropertyModel}
     * using the provided expression.
     *
     * @param name The non-null name of this component
     * @param model the instance of {@link IModel} from which the model object will be
     *            used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public AbstractLink(String name, IModel model, String expression)
    {
        super(name, model, expression);
    }

    /**
     * Constructor that uses the provided object as a simple model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}.
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public AbstractLink(String name, Serializable object)
    {
        super(name, object);
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object will be
     * wrapped in an instance of {@link wicket.model.Model} that will be wrapped in an instance of
     * {@link wicket.model.PropertyModel} using the provided expression.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public AbstractLink(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
     * Called when a link is clicked.
     * @see ILinkListener
     */
    public abstract void linkClicked();

    /**
     * Gets the url to use for this link.
     * @param cycle Request cycle
     * @return The URL that this link links to
     */
    protected String getURL(final RequestCycle cycle)
    {
        return cycle.urlFor(AbstractLink.this, ILinkListener.class);
    }


    /**
     * Gets the popup specification. If not-null, a javascript on-click event handler
     * will be generated that opens a new window using the popup properties.
     * @return the popup specification.
     */
    public final PopupSpecification getPopupSpecification()
    {
        return popupSpecification;
    }

    /**
     * Sets the popup specification. If not-null, a javascript on-click event handler
     * will be generated that opens a new window using the popup properties.
     * @param popupSpecification the popup specification.
     * @return This
     */
    public final AbstractLink setPopupSpecification(PopupSpecification popupSpecification)
    {
        this.popupSpecification = popupSpecification;
        return this;
    }
}