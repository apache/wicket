/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
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
package wicket.markup.html.link;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;

/**
 * Base class that that contains functionality for rendering disabled links.
 * 
 * @author Matej Knopp
 * @param <T>
 */
public abstract class AbstractLink<T> extends WebMarkupContainer<T>
{

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 */
	public AbstractLink(MarkupContainer parent, String id, IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 */
	public AbstractLink(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * Simple insertion string to allow disabled links to look like <i>Disabled
	 * link </i>.
	 */
	private String beforeDisabledLink;

	/**
	 * Simple insertion string to allow disabled links to look like <i>Disabled
	 * link </i>.
	 */
	private String afterDisabledLink;


	/**
	 * Sets the insertion string to allow disabled links to look like
	 * <i>Disabled link </i>.
	 * 
	 * @param afterDisabledLink
	 *            The insertion string
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
	 * Gets the insertion string to allow disabled links to look like
	 * <i>Disabled link </i>.
	 * 
	 * @return The insertion string
	 */
	public String getAfterDisabledLink()
	{
		return afterDisabledLink;
	}

	/**
	 * Sets the insertion string to allow disabled links to look like
	 * <i>Disabled link </i>.
	 * 
	 * @param beforeDisabledLink
	 *            The insertion string
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
	 * @see wicket.Component#internalOnAttach()
	 */
	@Override
	protected void internalOnAttach()
	{
		// Set default for before/after link text
		if (beforeDisabledLink == null)
		{
			final Application app = getApplication();
			beforeDisabledLink = app.getMarkupSettings().getDefaultBeforeDisabledLink();
			afterDisabledLink = app.getMarkupSettings().getDefaultAfterDisabledLink();
		}
	}

	/**
	 * Gets the insertion string to allow disabled links to look like
	 * <i>Disabled link </i>.
	 * 
	 * @return The insertion string
	 */
	public String getBeforeDisabledLink()
	{
		return beforeDisabledLink;
	}

	/**
	 * Helper methods that both checks whether the link is enabled and whether
	 * the action ENABLE is allowed.
	 * 
	 * @return whether the link should be rendered as enabled
	 */
	protected final boolean isLinkEnabled()
	{
		return isEnabled() && isEnableAllowed();
	}

	/**
	 * Renders this link's body.
	 * 
	 * @param markupStream
	 *            the markup stream
	 * @param openTag
	 *            the open part of this tag
	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	@Override
	protected final void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		// Draw anything before the body?
		if (!isLinkEnabled() && getBeforeDisabledLink() != null)
		{
			getResponse().write(getBeforeDisabledLink());
		}

		// Render the body of the link
		renderComponentTagBody(markupStream, openTag);

		// Draw anything after the body?
		if (!isLinkEnabled() && getAfterDisabledLink() != null)
		{
			getResponse().write(getAfterDisabledLink());
		}
	}

	/**
	 * Alters the tag so that the link renders as disabled.
	 * 
	 * This method is meant to be called from
	 * {@link #onComponentTag(ComponentTag)} method of the derived class.
	 * 
	 * @param tag
	 */
	protected void disableLink(final ComponentTag tag)
	{
		// if the tag is an anchor proper
		if (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("link")
				|| tag.getName().equalsIgnoreCase("area"))
		{
			// Change anchor link to span tag
			tag.setName("span");

			// Remove any href from the old link
			tag.remove("href");

			tag.remove("onclick");
		}
	}
}
