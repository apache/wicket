/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.link;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnEventHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;

/**
 * <p>
 * A simple anchor link (&lt;a href="http://url"&gt;) pointing to any URL. Usually this is used for
 * links to destinations outside of Wicket.
 * </p>
 * 
 * <p>
 * <strong>Note</strong>: in the case when the support for cookies in the browser is disabled the
 * user's jsessionid will leak in the 'Referrer' header after clicking this link. If this is a
 * problem for the application then better use a {@link Link} which redirects to a shared resource
 * (see
 * {@link WebApplication#mountResource(String, org.apache.wicket.request.resource.ResourceReference)}
 * , e.g. "/myapp/redirecting-resource?url=...") which on its side redirects to the new URL using
 * {@link RedirectToUrlException}. Another option is to use <code>rel="noreferrer"</code> attribute
 * in your markup but this will work only in the modern browsers (supporting HTML5 standard).
 * 
 * @author Juergen Donnerstag
 */
public class ExternalLink extends AbstractLink
{
	private static final long serialVersionUID = 1L;

	private boolean contextRelative = false;

	/**
	 * The popup specification. If not-null, a javascript on-click event handler will be generated
	 * that opens a new window using the popup properties.
	 */
	private PopupSettings popupSettings = null;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param href
	 *            the href attribute to set
	 * @param label
	 *            the label (body)
	 */
	public ExternalLink(final String id, final String href, final String label)
	{
		super(id);

		setDefaultModel(href != null ? new Model<>(href) : null);
		setBody(Model.of(label));
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            The name of this component
	 * @param href
	 *            the href attribute to set
	 */
	public ExternalLink(final String id, final String href)
	{
		this(id, Model.of(href));
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            The name of this component
	 * @param href
	 *            the href attribute to set
	 */
	public ExternalLink(final String id, final IModel<String> href)
	{
		this(id, href, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param href
	 *            the href attribute to set
	 * @param label
	 *            the label (body)
	 */
	public ExternalLink(final String id, final IModel<String> href, final IModel<?> label)
	{
		super(id);

		setDefaultModel(wrap(href));
		setBody(label);
	}

	/**
	 * Gets the popup specification. If not-null, a javascript on-click event handler will be
	 * generated that opens a new window using the popup properties.
	 * 
	 * @return the popup specification.
	 */
	public final PopupSettings getPopupSettings()
	{
		return popupSettings;
	}

	/**
	 * Sets the popup specification. If not-null, a javascript on-click event handler will be
	 * generated that opens a new window using the popup properties.
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
	 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		if (isEnabledInHierarchy() == false)
		{
			disableLink(tag);
		}
		else if (getDefaultModel() != null)
		{
			String url = renderUrl();
			// if the tag is an anchor proper
			if (url != null
				&& (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("link")
					|| tag.getName().equalsIgnoreCase("area")))
			{
				// generate the href attribute
				tag.put("href", url);
			}
		}
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		if (!isEnabledInHierarchy())
		{
			return;
		}

		String url = renderUrl();
		if (url != null)
		{
			if (popupSettings != null)
			{
				popupSettings.setTarget("'" + url + "'");
				response.render(OnEventHeaderItem.forComponent(this, "click",
					popupSettings.getPopupJavaScript()));
				return;
			}

			ComponentTag tag = getMarkupTag();
			// finally, when the tag is not a normal link
			if (!(tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("link")
				|| tag.getName().equalsIgnoreCase("area")
				|| tag.getName().equalsIgnoreCase("script")
				|| tag.getName().equalsIgnoreCase("style")))
			{
				// generate an onclick JS handler directly
				// in firefox when the element is quickly clicked 3 times a second request is
				// generated during page load. This check ensures that the click is ignored
				response.render(OnEventHeaderItem.forComponent(this, "click",
					"var win = this.ownerDocument.defaultView || this.ownerDocument.parentWindow; "
						+ "if (win == window) { window.location.href='" + url
						+ "'; } ;return false"));
				return;
			}
		}
	}

	/**
	 * @return the URL for this link
	 */
	private String renderUrl()
	{
		Object hrefValue = getDefaultModelObject();
		if (hrefValue == null)
		{
			return null;
		}

		String url = hrefValue.toString();
		if (contextRelative)
		{
			if (url.length() > 0 && url.charAt(0) == '/')
			{
				url = url.substring(1);
			}
			url = UrlUtils.rewriteToContextRelative(url, RequestCycle.get());
		}
		return url;
	}

	/**
	 * @return True if this link is automatically prepended with ../ to make it relative to the
	 *         context root.
	 */
	public boolean isContextRelative()
	{
		return contextRelative;
	}

	/**
	 * Set to true if this link should be automatically prepended with ../ to make it relative to
	 * the context root.
	 * 
	 * @param contextRelative
	 * @return This for chaining
	 */
	public ExternalLink setContextRelative(final boolean contextRelative)
	{
		this.contextRelative = contextRelative;
		return this;
	}
}
