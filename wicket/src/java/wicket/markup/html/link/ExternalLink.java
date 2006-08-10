/*
 * $Id: ExternalLink.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu, 25 May
 * 2006) $
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

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * A simple anchor link (&lt;a href="http://url"&gt;) pointing to any URL.
 * Usually this is used for links to destinations outside of Wicket.
 * 
 * @author Juergen Donnerstag
 */
public class ExternalLink extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/** the href attribute. */
	private final IModel href;

	/** this links' label. */
	private final IModel label;

	/**
	 * The popup specification. If not-null, a javascript on-click event handler
	 * will be generated that opens a new window using the popup properties.
	 */
	private PopupSettings popupSettings = null;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param href
	 *            the href attribute to set
	 * @param label
	 *            the label (body)
	 */
	public ExternalLink(MarkupContainer parent, final String id, final String href,
			final String label)
	{
		super(parent, id);

		this.href = (href != null ? new Model<String>(href) : null);
		this.label = (label != null ? new Model<String>(label) : null);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            The name of this component
	 * @param href
	 *            the href attribute to set
	 */
	public ExternalLink(MarkupContainer parent, final String id, final String href)
	{
		this(parent, id, href, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param href
	 *            the href attribute to set
	 * @param label
	 *            the label (body)
	 */
	public ExternalLink(MarkupContainer parent, final String id, final IModel href,
			final IModel label)
	{
		super(parent, id);

		this.href = href;
		this.label = label;
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            The name of this component
	 * @param href
	 *            the href attribute to set
	 */
	public ExternalLink(MarkupContainer parent, final String id, final IModel href)
	{
		this(parent, id, href, null);
	}

	/**
	 * Gets the popup specification. If not-null, a javascript on-click event
	 * handler will be generated that opens a new window using the popup
	 * properties.
	 * 
	 * @return the popup specification.
	 */
	public final PopupSettings getPopupSettings()
	{
		return popupSettings;
	}

	/**
	 * Sets the popup specification. If not-null, a javascript on-click event
	 * handler will be generated that opens a new window using the popup
	 * properties.
	 * 
	 * @param popupSettings
	 *            the popup specification.
	 * @return This
	 */
	public final ExternalLink setPopupSettings(final PopupSettings popupSettings)
	{
		this.popupSettings = popupSettings;
		return this;
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		if (href != null)
		{
			Object hrefValue = href.getObject();
			if (hrefValue != null)
			{
				String url = hrefValue.toString();
				// if the tag is an anchor proper
				if (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("link")
						|| tag.getName().equalsIgnoreCase("area"))
				{
					// generate the href attribute
					tag.put("href", url);

					// Add any popup script
					if (popupSettings != null)
					{
						// NOTE: don't encode to HTML as that is not valid
						// JavaScript
						tag.put("onclick", popupSettings.getPopupJavaScript());
					}
				}
				else
				{
					// generate a popup script by asking popup settings for one
					if (popupSettings != null)
					{
						popupSettings.setTarget("'" + url + "'");
						String popupScript = popupSettings.getPopupJavaScript();
						tag.put("onclick", popupScript);
					}
					else
					{
						// or generate an onclick JS handler directly
						tag.put("onclick", "location.href='" + url + "';");
					}
				}
			}
		}
	}

	/**
	 * Handle the container's body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		this.checkComponentTag(openTag, "a");
		if ((label != null) && (label.getObject() != null))
		{
			replaceComponentTagBody(markupStream, openTag, label.getObject().toString());
		}
		else
		{
			super.onComponentTagBody(markupStream, openTag);
		}
	}
}