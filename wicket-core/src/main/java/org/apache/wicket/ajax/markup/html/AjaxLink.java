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

import org.apache.wicket.Component;
import org.apache.wicket.IGenericComponent;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableConsumer;

/**
 * A component that allows a trigger request to be triggered via html anchor tag
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            type of model object
 * 
 */
public abstract class AjaxLink<T> extends AbstractLink implements IAjaxLink, IGenericComponent<T, AjaxLink<T>>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public AjaxLink(final String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public AjaxLink(final String id, final IModel<T> model)
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

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				AjaxLink.this.updateAjaxAttributes(attributes);
			}
			
			@Override
			public boolean getStatelessHint(Component component)
			{
				return AjaxLink.this.getStatelessHint();
			}
		};
	}

	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		if (isEnabledInHierarchy())
		{
			String tagName = tag.getName();
			
			if (tagName.equalsIgnoreCase("a") || tagName.equalsIgnoreCase("link") ||
				tagName.equalsIgnoreCase("area"))
			{
				// disable any href attr in markup
				tag.put("href", "javascript:;");
			}
			else if (tagName.equalsIgnoreCase("button"))
			{
				// WICKET-5597 prevent submit
				tag.put("type", "button");
			}
		}
		else
		{
			disableLink(tag);
		}

	}

	/**
	 * Listener method invoked on the ajax request generated when the user clicks the link
	 * 
	 * @param target
	 */
	@Override
	public abstract void onClick(final AjaxRequestTarget target);

	/**
	 * Creates an {@link AjaxLink} based on lambda expressions
	 * 
	 * @param id
	 *            the id of the ajax link
	 * @param onClick
	 *            the {@code SerializableConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxLink}
	 */
	public static <T> AjaxLink<T> onClick(String id,
		SerializableConsumer<AjaxRequestTarget> onClick)
	{
		Args.notNull(onClick, "onClick");

		return new AjaxLink<T>(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				onClick.accept(target);
			}
		};
	}

	/**
	 * Creates an {@link AjaxLink} based on lambda expressions
	 * 
	 * @param id
	 *            the id of the ajax link
	 * @param onClick
	 *            the consumer of the clicked link and an {@link AjaxRequestTarget}
	 * @return the {@link AjaxLink}
	 */
	public static <T> AjaxLink<T> onClick(String id,
		SerializableBiConsumer<AjaxLink<T>, AjaxRequestTarget> onClick)
	{
		Args.notNull(onClick, "onClick");

		return new AjaxLink<T>(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				onClick.accept(this, target);
			}
		};
	}
	@Override
	protected boolean getStatelessHint()
	{
		return false;
	}
}
