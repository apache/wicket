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
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ajax link that will degrade to a normal request if ajax is not available or javascript is
 * disabled
 *
 *  <p>
 * If JavaScript is enabled then the registered JavaScript event 'click' handler will be used,
 * otherwise the 'href' attribute if the markup element is an &lt;a&gt;, &lt;area&gt; or &lt;link&gt;.
 * AjaxFallbackLink doesn't fallback if the markup element is none of the three above.
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
	private static final Logger LOG = LoggerFactory.getLogger(AjaxFallbackLink.class);

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
		add(newAjaxEventBehavior("onclick"));
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

			/**
			 * 
			 * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
			 */
			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				onClick(target);
			}

			/**
			 * 
			 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getAjaxCallDecorator()
			 */
			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return new CancelEventIfNoAjaxDecorator(
					AjaxFallbackLink.this.getAjaxCallDecorator());
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
		};
	}

	/**
	 * @return the channel that manages how Ajax calls are executed
	 * @see AbstractDefaultAjaxBehavior#getChannel()
	 */
	protected AjaxChannel getChannel()
	{
		return null;
	}

	/**
	 * 
	 * @return call decorator to use or null if none
	 */
	protected IAjaxCallDecorator getAjaxCallDecorator()
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
	public abstract void onClick(final AjaxRequestTarget target);

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		String tagName = tag.getName();
		if (
			LOG.isWarnEnabled() &&
			!("a".equalsIgnoreCase(tagName) || "area".equalsIgnoreCase(tagName) || "link".equalsIgnoreCase(tagName))
		)
		{
			LOG.warn("{} must be used only with <a>, <area> or <link> markup elements. The fallback functionality doesn't" +
					" work for other markup elements. Component path: {}, markup element: <{}>.",
					new Object[] {AjaxFallbackLink.class.getSimpleName(), getClassRelativePath(), tagName});
		}
	}
}
