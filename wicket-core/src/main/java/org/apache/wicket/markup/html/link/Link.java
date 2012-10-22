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

import org.apache.wicket.Component;
import org.apache.wicket.IGenericComponent;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Implementation of a hyperlink component. A link can be used with an anchor (&lt;a href...)
 * element or any element that supports the onclick javascript event handler (such as buttons, td
 * elements, etc). When used with an anchor, a href attribute will be generated. When used with any
 * other element, an onclick javascript event handler attribute will be generated.
 * <p>
 * You can use a link like:
 * 
 * <pre>
 * add(new Link(&quot;myLink&quot;)
 * {
 *     public void onClick()
 *     {
 *         // do something here...
 *     }
 * );
 * </pre>
 * 
 * and in your HTML file:
 * 
 * <pre>
 *  &lt;a href=&quot;#&quot; wicket:id=&quot;myLink&quot;&gt;click here&lt;/a&gt;
 * </pre>
 * 
 * or:
 * 
 * <pre>
 *  &lt;td wicket:id=&quot;myLink&quot;&gt;my clickable column&lt;/td&gt;
 * </pre>
 * 
 * </p>
 * The following snippet shows how to pass a parameter from the Page creating the Page to the Page
 * responded by the Link.
 * 
 * <pre>
 * add(new Link&lt;MyObject&gt;(&quot;link&quot;, listItem.getModel())
 * {
 *     public void onClick()
 *     {
 *         MyObject obj = getModelObject();
 *         setResponsePage(new MyPage(obj));
 *     }
 * </pre>
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @param <T>
 *            type of model object
 */
public abstract class Link<T> extends AbstractLink implements ILinkListener, IGenericComponent<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * An anchor (form 'http://server/app/etc#someAnchor') will be appended to the link so that
	 * after this link executes, it will jump to the provided anchor component's position. The
	 * provided anchor must either have the {@link Component#getOutputMarkupId()} flag true, or it
	 * must be attached to a &lt;a tag with a href attribute of more than one character starting
	 * with '#' ('&lt;a href="#someAnchor" ... ').
	 */
	private Component anchor;

	/**
	 * True if link should automatically enable/disable based on current page; false by default.
	 */
	private boolean autoEnable = false;

	/**
	 * The popup specification. If not-null, a javascript on-click event handler will be generated
	 * that opens a new window using the popup properties.
	 */
	private PopupSettings popupSettings = null;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public Link(final String id)
	{
		super(id);
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Link(final String id, IModel<T> model)
	{
		super(id, model);
	}

	/**
	 * Gets any anchor component.
	 * 
	 * @return Any anchor component to jump to, might be null
	 */
	public Component getAnchor()
	{
		return anchor;
	}

	/**
	 * Gets whether link should automatically enable/disable based on current page.
	 * 
	 * @return Whether this link should automatically enable/disable based on current page.
	 */
	public final boolean getAutoEnable()
	{
		return autoEnable;
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
	 * @see org.apache.wicket.Component#isEnabled()
	 */
	@Override
	public boolean isEnabled()
	{
		// If we're auto-enabling
		if (getAutoEnable())
		{
			// the link is enabled if this link doesn't link to the current page
			return !linksTo(getPage());
		}
		return super.isEnabled();
	}

	/**
	 * @see org.apache.wicket.Component#getStatelessHint()
	 */
	@Override
	protected boolean getStatelessHint()
	{
		return false;
	}

	/**
	 * Called when a link is clicked.
	 */
	public abstract void onClick();

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET API. DO NOT ATTEMPT TO OVERRIDE OR CALL IT.
	 * 
	 * Called when a link is clicked. The implementation of this method is currently to simply call
	 * onClick(), but this may be augmented in the future.
	 * 
	 * @see ILinkListener
	 */
	@Override
	public final void onLinkClicked()
	{
		// Invoke subclass handler
		onClick();
	}

	/**
	 * Sets an anchor component. An anchor (form 'http://server/app/etc#someAnchor') will be
	 * appended to the link so that after this link executes, it will jump to the provided anchor
	 * component's position. The provided anchor must either have the
	 * {@link Component#getOutputMarkupId()} flag true, or it must be attached to a &lt;a tag with a
	 * href attribute of more than one character starting with '#' ('&lt;a href="#someAnchor" ...
	 * ').
	 * 
	 * @param anchor
	 *            The anchor
	 * @return this
	 */
	public Link<T> setAnchor(Component anchor)
	{
		addStateChange();
		this.anchor = anchor;
		return this;
	}

	/**
	 * Sets whether this link should automatically enable/disable based on current page.
	 * 
	 * @param autoEnable
	 *            whether this link should automatically enable/disable based on current page.
	 * @return This
	 */
	public final Link<T> setAutoEnable(final boolean autoEnable)
	{
		this.autoEnable = autoEnable;
		return this;
	}

	/**
	 * Sets the popup specification. If not-null, a javascript on-click event handler will be
	 * generated that opens a new window using the popup properties.
	 * 
	 * @param popupSettings
	 *            the popup specification.
	 * @return This
	 */
	public final Link<T> setPopupSettings(final PopupSettings popupSettings)
	{
		this.popupSettings = popupSettings;
		return this;
	}

	/**
	 * Appends any anchor to the url if the url is not null and the url does not already contain an
	 * anchor (url.indexOf('#') != -1). This implementation looks whether an anchor component was
	 * set, and if so, it will append the markup id of that component. That markup id is gotten by
	 * either calling {@link Component#getMarkupId()} if {@link Component#getOutputMarkupId()}
	 * returns true, or if the anchor component does not output it's id, this method will try to
	 * retrieve the id from the markup directly. If neither is found, an
	 * {@link WicketRuntimeException exception} is thrown. If no anchor component was set, but the
	 * link component is attached to a &lt;a element, this method will append what is in the href
	 * attribute <i>if</i> there is one, starts with a '#' and has more than one character.
	 * <p>
	 * You can override this method, but it means that you have to take care of whatever is done
	 * with any set anchor component yourself. You also have to manually append the '#' at the right
	 * place.
	 * </p>
	 * 
	 * @param tag
	 *            The component tag
	 * @param url
	 *            The url to start with
	 * @return The url, possibly with an anchor appended
	 */
	protected CharSequence appendAnchor(final ComponentTag tag, CharSequence url)
	{
		if (url != null)
		{
			Component anchor = getAnchor();
			if (anchor != null)
			{
				if (url.toString().indexOf('#') == -1)
				{
					String id;
					if (anchor.getOutputMarkupId())
					{
						id = anchor.getMarkupId();
					}
					else
					{
						id = anchor.getMarkupAttributes().getString("id");
					}

					if (id != null)
					{
						url = url + "#" + anchor.getMarkupId();
					}
					else
					{
						throw new WicketRuntimeException("an achor component was set on " + this +
							" but it neither has outputMarkupId set to true " +
							"nor has a id set explicitly");
					}
				}
			}
			else
			{
				if (tag.getName().equalsIgnoreCase("a"))
				{
					if (url.toString().indexOf('#') == -1)
					{
						String href = tag.getAttributes().getString("href");
						if (href != null && href.length() > 1 && href.charAt(0) == '#')
						{
							url = url + href;
						}
					}
				}
			}
		}
		return url;
	}

	/**
	 * @param url
	 *            The url for the link
	 * @return Any onClick JavaScript that should be used
	 */
	protected CharSequence getOnClickScript(final CharSequence url)
	{
		return null;
	}

	/**
	 * Gets the url to use for this link.
	 * 
	 * @return The URL that this link links to
	 */
	protected CharSequence getURL()
	{
		return urlFor(ILinkListener.INTERFACE, new PageParameters());
	}

	/**
	 * Whether this link refers to the given page.
	 * 
	 * @param page
	 *            A page
	 * @return True if this link goes to the given page
	 */
	protected boolean linksTo(final Page page)
	{
		return false;
	}

	/**
	 * Handles this link's tag. OVERRIDES MUST CALL SUPER.
	 * 
	 * @param tag
	 *            the component tag
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for tag
		super.onComponentTag(tag);

		// If we're disabled
		if (!isLinkEnabled())
		{
			disableLink(tag);
		}
		else
		{
			// Set href to link to this link's linkClicked method
			CharSequence url = getURL();

			// append any anchor
			url = appendAnchor(tag, url);

			// if the tag is an anchor proper
			if (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("link") ||
				tag.getName().equalsIgnoreCase("area"))
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
			else if (tag.getName().equalsIgnoreCase("script") ||
				tag.getName().equalsIgnoreCase("style"))
			{
				tag.put("src", url);
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
					// in firefox when the element is quickly clicked 3 times a second request is
					// generated during page load. This check ensures that the click is ignored
					tag.put(
						"onclick",
						"var win = this.ownerDocument.defaultView || this.ownerDocument.parentWindow; " +
							"if (win == window) { window.location.href='" +
							url +
							"'; } ;return false");
				}
			}


			// If the subclass specified javascript, use that
			final CharSequence onClickJavaScript = getOnClickScript(url);
			if (onClickJavaScript != null)
			{
				tag.put("onclick", onClickJavaScript);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public final IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	@Override
	public final void setModel(IModel<T> model)
	{
		setDefaultModel(model);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	@Override
	public final void setModelObject(T object)
	{
		setDefaultModelObject(object);
	}

}
