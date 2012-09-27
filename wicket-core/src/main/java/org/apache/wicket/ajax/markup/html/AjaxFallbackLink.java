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
package org.apache.wicket.ajax.markup.html;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

/**
 * An ajax link that will degrade to a normal request if ajax is not available or javascript is
 * disabled.
 * 
 * <p>
 * If JavaScript is enabled then the registered JavaScript event 'click' handler will be used,
 * otherwise the 'href' attribute if the markup element is an &lt;a&gt;, &lt;area&gt; or
 * &lt;link&gt;. AjaxFallbackLink doesn't fallback if the markup element is none of the three above.
 * </p>
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            type of model object
 */
public abstract class AjaxFallbackLink<T> extends Link<T> implements IAjaxLink
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public AjaxFallbackLink(final String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public AjaxFallbackLink(final String id, final IModel<T> model)
	{
		super(id, model);

	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		add(newAjaxEventBehavior("click"));
	}

	/**
	 * @param event
	 *            the name of the default event on which this link will listen to
	 * @return the ajax behavior which will be executed when the user clicks the link
	 */
	protected AjaxEventBehavior newAjaxEventBehavior(String event)
	{
		return new AjaxEventBehavior(event)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				onClick(target);
			}

			/**
			 * 
			 * @see org.apache.wicket.ajax.AjaxEventBehavior#onComponentTag(org.apache.wicket.markup.ComponentTag)
			 */
			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				// only render handler if link is enabled
				if (isLinkEnabled())
				{
					super.onComponentTag(tag);
				}
			}

			@Override
			protected AjaxChannel getChannel()
			{
				return AjaxFallbackLink.this.getChannel();
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				AjaxFallbackLink.this.updateAjaxAttributes(attributes);
			}
		};
	}

	/**
	 * @param attributes
	 */
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}

	/**
	 * @return the channel that manages how Ajax calls are executed
	 * @see AbstractDefaultAjaxBehavior#getChannel()
	 */
	@Deprecated
	protected AjaxChannel getChannel()
	{
		return null;
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public final void onClick()
	{
		onClick(null);
	}

	/**
	 * Callback for the onClick event. If ajax failed and this event was generated via a normal link
	 * the target argument will be null
	 * 
	 * @param target
	 *            ajax target if this linked was invoked using ajax, null otherwise
	 */
	@Override
	public abstract void onClick(final AjaxRequestTarget target);

	/**
	 * Removes any inline 'onclick' attributes set by Link#onComponentTag(ComponentTag).
	 * 
	 * @param tag
	 *            the component tag
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		// Ajax links work with JavaScript Event registration
		tag.remove("onclick");

		String tagName = tag.getName();
		if (isLinkEnabled() &&
			!("a".equalsIgnoreCase(tagName) || "area".equalsIgnoreCase(tagName) || "link".equalsIgnoreCase(tagName)))
		{
			String msg = String.format(
				"%s must be used only with <a>, <area> or <link> markup elements. "
					+ "The fallback functionality doesn't work for other markup elements. "
					+ "Component path: %s, markup element: <%s>.",
				AjaxFallbackLink.class.getSimpleName(), getClassRelativePath(), tagName);
			findMarkupStream().throwMarkupException(msg);
		}
	}
}
