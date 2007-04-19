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

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

/**
 * An ajax link that will degrade to a normal request if ajax is not available
 * or javascript is disabled
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxFallbackLink extends Link implements IAjaxLink
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
	public AjaxFallbackLink(final String id, final IModel model)
	{
		super(id, model);

		add(new AjaxEventBehavior("onclick")
		{
			private static final long serialVersionUID = 1L;

			protected void onEvent(AjaxRequestTarget target)
			{
				onClick(target);
			}

			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return new CancelEventIfNoAjaxDecorator(AjaxFallbackLink.this
						.getAjaxCallDecorator());
			}
			
			protected void onComponentTag(ComponentTag tag)
			{
				// only render handler if link is enabled
				if (isLinkEnabled())
				{
					super.onComponentTag(tag);
				}
			}
		});
	}

	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return null;
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.html.link.Link#onClick()
	 */
	public final void onClick()
	{
		onClick(null);
	}

	/**
	 * Callback for the onClick event. If ajax failed and this event was
	 * generated via a normal link the target argument will be null
	 * 
	 * @param target
	 *            ajax target if this linked was invoked using ajax, null
	 *            otherwise
	 */
	public abstract void onClick(final AjaxRequestTarget target);
}
