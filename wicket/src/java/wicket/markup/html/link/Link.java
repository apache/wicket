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

import wicket.Container;
import wicket.Page;
import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlContainer;


/**
 * Implementation of a hyperlink component.
 * @author Jonathan Locke
 */
public abstract class Link extends HtmlContainer implements ILinkListener
{
    static
    {
        // Allow calls through the ILinkListener interface
        RequestCycle.registerListenerInterface(ILinkListener.class);
    }

    //--------------------------------------------------------------------------------
    //----------------------------------- properties
    // ---------------------------------
    //--------------------------------------------------------------------------------
    // True if link should automatically enable/disable based on current page
    private boolean autoEnable = true;

    // Simple insertion strings to allow disabled links to look like <i>Disabled
    // link</i>
    private String beforeDisabledLink;

    private String afterDisabledLink;

    // True if this link is enabled
    private boolean enabled = true;

    /**
     * The popup specs; if not-null, a javascript on-click event handler will be generated
     * that opens a new window using the popup properties.
     */
    private PopupSpecification popupSpecification = null;

    /**
     * Constructor.
     * @param componentName The name of the component
     */
    public Link(final String componentName)
    {
        super(componentName);
    }

    /**
     * Called when a link is clicked.
     * @see ILinkListener
     * @param cycle The cycle object
     */
    public abstract void linkClicked(final RequestCycle cycle);

    /**
     * Whether link should automatically enable/disable based on current page
     * @return Returns True if link should automatically enable/disable based on current
     *         page.
     */
    public final boolean getAutoEnable()
    {
        return autoEnable;
    }

    /**
     * Set whether this link should automatically enable/disable based on current page.
     * @param autoEnable whether this link should automatically enable/disable based on
     *            current page.
     * @return This
     */
    public final Link setAutoEnable(final boolean autoEnable)
    {
        this.autoEnable = autoEnable;

        return this;
    }

    /**
     * Whether this link is enabled.
     * @return Returns whether this link is enabled.
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
     * @param page A page
     * @return True if this link goes to the given page
     */
    protected boolean linksTo(final Page page)
    {
        return false;
    }

    /**
     * @param cycle Request cycle
     * @return The URL that this link links to
     */
    String getURL(final RequestCycle cycle)
    {
        return cycle.urlFor(Link.this, ILinkListener.class);
    }

    /**
     * Set the after disabled link.
     * @param afterDisabledLink The afterDisabledLink to set.
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
     * @param beforeDisabledLink The beforeDisabledLink to set.
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
     * Get popupSpecification.
     * @return popupSpecification.
     */
    public final PopupSpecification getPopupSpecification()
    {
        return popupSpecification;
    }

    /**
     * Set popupSpecification.
     * @param popupSpecification popupSpecification.
     * @return This
     */
    public final Link setPopupSpecification(PopupSpecification popupSpecification)
    {
        this.popupSpecification = popupSpecification;

        return this;
    }

    //--------------------------------------------------------------------------------
    //----------------------------------- rendering
    // ----------------------------------
    //--------------------------------------------------------------------------------

    /**
     * @see wicket.Component#handleBody(RequestCycle, MarkupStream,
     *      ComponentTag)
     */
    protected final void handleBody(final RequestCycle cycle, final MarkupStream markupStream,
            final ComponentTag openTag)
    {
        // Get disabled component of the same name with "Disabled" appended
        final Container disabledContainer = (Container) get("disabled");

        if (disabledContainer != null)
        {
            // Get enabled container
            final Container enabledContainer = (DisabledLink) get("enabled");

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
        if (!enabled && (beforeDisabledLink != null))
        {
            cycle.getResponse().write(beforeDisabledLink);
        }

        // Render the body of the link
        renderBody(cycle, markupStream, openTag);

        // Draw anything after the body?
        if (!enabled && (afterDisabledLink != null))
        {
            cycle.getResponse().write(afterDisabledLink);
        }
    }

    /**
     * @see wicket.Component#handleComponentTag(RequestCycle, ComponentTag)
     */
    protected final void handleComponentTag(final RequestCycle cycle, final ComponentTag tag)
    {
        // Can only attach links to anchor tags
        checkTag(tag, "a");

        // Default handling for tag
        super.handleComponentTag(cycle, tag);

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
            tag.put("href", getURL(cycle));
        }

        // Add any popup script
        if (popupSpecification != null)
        {
            final String popupScript = popupSpecification.getPopupJavaScript();

            tag.put("onClick", popupScript);
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
