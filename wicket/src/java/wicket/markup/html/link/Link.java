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

import wicket.Container;
import wicket.Page;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.model.IModel;

/**
 * Implementation of a hyperlink component. A link must be used with an anchor (&lt;a href...)
 * element.
 * <p>
 * You can use a link like:
 * <pre>
 * add(new Link("myLink"){
 *
 *   public void linkClicked(RequestCycle cycle)
 *   {
 *      // do something here...  
 *   }
 * );
 * </pre>
 * and in your HTML file:
 * <pre>
 *  &lt;a href="#" id="wicket-myLink"&gt;click here&lt;/a&gt;
 * </pre>
 * </p>
 *
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public abstract class Link extends AbstractLink
{
    /** True if link should automatically enable/disable based on current page. */
    private boolean autoEnable = true;

    /** 
     * Simple insertion string to allow disabled links to
     * look like <i>Disabled link</i>.
     */
    private String beforeDisabledLink;

    /** 
     * Simple insertion string to allow disabled links to
     * look like <i>Disabled link</i>.
     */
    private String afterDisabledLink;

    /** True if this link is enabled. */
    private boolean enabled = true;

    /**
     * Construct.
     * @param componentName the name of the component
     */
    public Link(String componentName)
    {
        super(componentName);
    }

    /**
     * Constructor that uses the provided {@link IModel}as its model.
     * @param name The non-null name of this component
     * @param model the model
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public Link(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel}as a dynamic model.
     * This model will be wrapped in an instance of {@link wicket.model.PropertyModel}using the
     * provided expression.
     * @param name The non-null name of this component
     * @param model the instance of {@link IModel}from which the model object will be
     *            used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public Link(String name, IModel model, String expression)
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
    public Link(String name, Serializable object)
    {
        super(name, object);
    }

    /**
     * Constructor that uses the provided object as a dynamic model. This object will be
     * wrapped in an instance of {@link wicket.model.Model}that will be wrapped in an instance
     * of {@link wicket.model.PropertyModel}using the provided expression.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws wicket.RenderException Thrown if the component has been given a null name.
     */
    public Link(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
     * Gets whether link should automatically enable/disable based on current page.
     * @return Whether this link should automatically enable/disable
     * based on current page.
     */
    public final boolean getAutoEnable()
    {
        return autoEnable;
    }

    /**
     * Sets whether this link should automatically enable/disable based on current page.
     * @param autoEnable whether this link should automatically enable/disable
     * 	based on current page.
     * @return This
     */
    public final Link setAutoEnable(final boolean autoEnable)
    {
        this.autoEnable = autoEnable;
        return this;
    }

    /**
     * Gets whether this link is enabled.
     * @return whether this link is enabled.
     */
    public final boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Sets link enabled state.
     * @param enabled The enabled to set.
     * @return This
     */
    public final Link setEnabled(final boolean enabled)
    {
        // Set enabled state
        this.enabled = enabled;
        return this;
    }

    /**
     * Whether this link refers to the given page.
     * @param page A page
     * @return True if this link goes to the given page
     */
    protected boolean linksTo(final Page page)
    {
        return false;
    }

    /**
     * Sets the insertion string to allow disabled links to look like <i>Disabled link</i>.
     * @param beforeDisabledLink The insertion string
     */
    public void setBeforeDisabledLink(final String beforeDisabledLink)
    {
        if (beforeDisabledLink == null)
        {
            throw new IllegalArgumentException(
                    "Value cannot be null.  For no text, specify an empty String instead.");
        }
        this.beforeDisabledLink = beforeDisabledLink;
    }

    /**
     * Gets the insertion string to allow disabled links to look like <i>Disabled link</i>.
     * @return The insertion string
     */
    public String getBeforeDisabledLink()
    {
    	return beforeDisabledLink;
    }

    /**
     * Sets the insertion string to allow disabled links to look like <i>Disabled link</i>.
     * @param afterDisabledLink The insertion string
     */
    public void setAfterDisabledLink(final String afterDisabledLink)
    {
        if (afterDisabledLink == null)
        {
            throw new IllegalArgumentException(
                    "Value cannot be null.  For no text, specify an empty String instead.");
        }
        this.afterDisabledLink = afterDisabledLink;
    }

    /**
     * Gets the insertion string to allow disabled links to look like <i>Disabled link</i>.
     * @return The insertion string
     */
    public String getAfterDisabledLink()
    {
    	return afterDisabledLink;
    }

    /**
     * Renders this link's body.
     * @param markupStream the markup stream
     * @param openTag the open part of this tag
     * @see wicket.Component#handleBody(MarkupStream, ComponentTag)
     */
    protected final void handleBody(final MarkupStream markupStream, final ComponentTag openTag)
    {
        // Get disabled component of the same name with "Disabled" appended
        final Container disabledContainer = (Container)get("disabled");

        if (disabledContainer != null)
        {
            // Get enabled container
            final Container enabledContainer = (Container)get("enabled");

            // Set visibility of enabled and disabled children
            enabledContainer.setVisible(enabled);
            disabledContainer.setVisible(!enabled);
        }

        // Set default for before/after link text
        if (beforeDisabledLink == null)
        {
            beforeDisabledLink = getApplicationSettings().getDefaultBeforeDisabledLink();
            afterDisabledLink = getApplicationSettings().getDefaultAfterDisabledLink();
        }

        // Draw anything before the body?
        if (!enabled && beforeDisabledLink != null)
        {
            getResponse().write(beforeDisabledLink);
        }

        // Render the body of the link
        renderBody(markupStream, openTag);

        // Draw anything after the body?
        if (!enabled && afterDisabledLink != null)
        {
            getResponse().write(afterDisabledLink);
        }
    }

    /**
     * Handles this link's tag.
     * @param tag the component tag
     * @see wicket.Component#handleComponentTag(ComponentTag)
     */
    protected final void handleComponentTag(final ComponentTag tag)
    {
        // Can only attach links to anchor tags
        checkTag(tag, "a");

        // Default handling for tag
        super.handleComponentTag(tag);

        // If we're auto-enabling
        if (autoEnable)
        {
            // the link is enabled if this link doesn't link to the current page
            setEnabled(!linksTo(getPage()));
        }

        // If we're disabled
        if (!enabled)
        {
            // Change anchor link to span tag
            tag.setName("span");

            // Remove any href from the old link
            tag.remove("href");
        }
        else
        {
            // Set href to link to this link's linkClicked method
			tag.put("href", getURL().replaceAll("&", "&amp;"));
        }

        // Add any popup script
        PopupSpecification popupSpecification = getPopupSpecification();
        if (popupSpecification != null)
        {
            tag.put("onClick", popupSpecification.getPopupJavaScript().replaceAll("&", "&amp;"));
        }
    }
}
