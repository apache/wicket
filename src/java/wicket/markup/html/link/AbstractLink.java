/*
 * $Id$
 * $Revision$ $Date$
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

import java.io.Serializable;

import wicket.RequestCycle;
import wicket.markup.html.WebMarkupContainer;

/**
 * Base class for links.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public abstract class AbstractLink extends WebMarkupContainer implements ILinkListener
{
	/**
	 * The popup specification. If not-null, a javascript on-click event handler
	 * will be generated that opens a new window using the popup properties.
	 */
	private PopupSettings popupSettings = null;

	/**
     * @see wicket.Component#Component(String)
	 */
	public AbstractLink(String componentName)
	{
		super(componentName);
	}

	/**
     * @see wicket.Component#Component(String, Serializable)
	 */
	public AbstractLink(String name, Serializable object)
	{
		super(name, object);
	}

	/**
     * @see wicket.Component#Component(String, Serializable, String)
	 */
	public AbstractLink(String name, Serializable object, String expression)
	{
		super(name, object, expression);
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
	 * THIS METHOD IS NOT PART OF THE WICKET API.  DO NOT ATTEMPT TO OVERRIDE 
	 * OR CALL IT.
	 * 
	 * Called when a link is clicked.  The implementation of this method is currently
	 * to simply call onClick(), but this may be augmented in the future.
	 * 
	 * @see ILinkListener
	 */
	public final void onLinkClicked()
	{
		onClick();
	}

	/**
	 * Called when a link is clicked.
	 */
	public abstract void onClick();

	/**
	 * Sets the popup specification. If not-null, a javascript on-click event
	 * handler will be generated that opens a new window using the popup
	 * properties.
	 * 
	 * @param popupSettings
	 *            the popup specification.
	 * @return This
	 */
	public final AbstractLink setPopupSettings(PopupSettings popupSettings)
	{
		this.popupSettings = popupSettings;
		return this;
	}

	/**
	 * Gets the url to use for this link.
	 * 
	 * @return The URL that this link links to
	 */
	protected String getURL()
	{
		return getRequestCycle().urlFor(AbstractLink.this, ILinkListener.class);
	}
    
	static
	{
		// Allow calls through the ILinkListener interface
		RequestCycle.registerRequestListenerInterface(ILinkListener.class);
	}
}