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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IComponentOnAfterRenderListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.EnclosureContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.parser.filter.EnclosureHandler;
import org.apache.wicket.markup.resolver.ComponentResolvers;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.response.NullResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An Enclosure are automatically created by Wicket. Do not create it yourself. An Enclosure
 * container is created if &lt;wicket:enclosure&gt; is found in the markup. It is meant to solve the
 * following situation. Instead of
 * 
 * <pre>
 *    &lt;table wicket:id=&quot;label-container&quot; class=&quot;notify&quot;&gt;&lt;tr&gt;&lt;td&gt;&lt;span wicket:id=&quot;label&quot;&gt;[[notification]]&lt;/span&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;
 *    WebMarkupContainer container=new WebMarkupContainer(&quot;label-container&quot;)
 *    {
 *       public boolean isVisible()
 *       {
 *           return hasNotification();
 *       }
 *    };
 *    add(container);
 *     container.add(new Label(&quot;label&quot;, notificationModel));
 * </pre>
 * 
 * with Enclosure you are able to do the following:
 * 
 * <pre>
 *    &lt;wicket:enclosure&gt;
 *      &lt;table class=&quot;notify&quot;&gt;&lt;tr&gt;&lt;td&gt;&lt;span wicket:id=&quot;label&quot;&gt;[[notification]]&lt;/span&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;
 *    &lt;/wicket:enclosure&gt;
 *    add(new Label(&quot;label&quot;, notificationModel))
 *    {
 *       public boolean isVisible()
 *       {
 *           return hasNotification();
 *       }
 *    }
 * </pre>
 * 
 * Please note that since a transparent auto-component is created for the tag, the markup and the
 * component hierarchy will not be in sync which leads to subtle differences if your code relies on
 * onBeforeRender() and validate() being called for the children inside the enclosure tag. E.g. it
 * might happen that onBeforeRender() and validate() gets called on invisible components. In doubt,
 * please fall back to {@link EnclosureContainer}.
 * 
 * @see EnclosureResolver
 * @see EnclosureHandler
 * @see EnclosureContainer
 * 
 * @author Juergen Donnerstag
 * @since 1.3
 */
public class Enclosure extends WebMarkupContainer implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(Enclosure.class);

	/** The child component to delegate the isVisible() call to */
	private Component childComponent;

	/** Id of the child component that will control visibility of the enclosure */
	private final CharSequence childId;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param childId
	 */
	public Enclosure(final String id, final CharSequence childId)
	{
		super(id);

		if (childId == null)
		{
			throw new MarkupException(
				"You most likely forgot to register the EnclosureHandler with the MarkupParserFactory");
		}

		this.childId = childId;
	}

	/**
	 * @see org.apache.wicket.Component#onInitialize()
	 */
	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		// enclosure's parent container
		MarkupContainer container = getEnclosureParent();

		// clear the cache
		childComponent = null;

		// get Child Component. If not "added", ask a resolver to find it.
		childComponent = getChildComponent(new MarkupStream(getMarkup()), container);
		checkChildComponent(childComponent);
	}

	/**
	 * Get the real parent container
	 * 
	 * @return enclosure's parent markup container
	 */
	private MarkupContainer getEnclosureParent()
	{
		MarkupContainer parent = getParent();
		while ((parent != null) && parent.isAuto())
		{
			parent = parent.getParent();
		}

		if (parent == null)
		{
			throw new WicketRuntimeException(
				"Unable to find parent component which is not a transparent resolver");
		}
		return parent;
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		// TODO this is where I wish we had something like "enum(TAG, BODY, NONE, ALL) isVisible()"
		// set the enclosure visibility
		boolean visible = childComponent.determineVisibility();

		// We want to know which components are rendered inside the enclosure
		final IComponentOnAfterRenderListener listener = new EnclosureListener(this);

		try
		{
			// register the listener
			getApplication().addComponentOnAfterRenderListener(listener);

			if (visible)
			{
				super.onComponentTagBody(markupStream, openTag);
			}
			else
			{
				RequestCycle cycle = getRequestCycle();
				Response response = cycle.getResponse();
				try
				{
					cycle.setResponse(NullResponse.getInstance());

					super.onComponentTagBody(markupStream, openTag);
				}
				finally
				{
					cycle.setResponse(response);
				}
			}
		}
		finally
		{
			// make sure we remove the listener
			getApplication().removeComponentOnAfterRenderListener(listener);
		}
	}

	/**
	 * @param markupStream
	 * @param container
	 * @return The component associated with the
	 */
	private Component getChildComponent(final MarkupStream markupStream, MarkupContainer container)
	{
		Component controller = container.get(childId.toString());
		if (controller == null)
		{
			int orgIndex = markupStream.getCurrentIndex();
			try
			{
				while (markupStream.hasMore())
				{
					markupStream.next();
					if (markupStream.skipUntil(ComponentTag.class))
					{
						ComponentTag tag = markupStream.getTag();
						if ((tag != null) && (tag.isOpen() || tag.isOpenClose()))
						{
							if (childId.equals(tag.getId()))
							{
								controller = ComponentResolvers.resolveByComponentHierarchy(
									container, markupStream, tag);
								break;
							}
						}
					}
				}
			}
			finally
			{
				markupStream.setCurrentIndex(orgIndex);
			}
		}
		return controller;
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
	 *      org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
	 */
	public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		if (childId.equals(tag.getId()))
		{
			return childComponent;
		}
		return getEnclosureParent().get(tag.getId());
	}

	/**
	 * 
	 * @param controller
	 */
	private void checkChildComponent(final Component controller)
	{
		if (controller == null)
		{
			throw new WicketRuntimeException("Could not find child with id: " + childId +
				" in the wicket:enclosure");
		}
		else if (controller == this)
		{
			throw new WicketRuntimeException(
				"Programming error: childComponent == enclose component; endless loop");
		}
	}

	/**
	 * Enclosure will register this listener during the body render phase of the Enclosure
	 */
	private static class EnclosureListener implements IComponentOnAfterRenderListener
	{
		private final Enclosure enclosure;

		private EnclosureListener(final Enclosure enclosure)
		{
			this.enclosure = enclosure;
		}

		/**
		 * @see org.apache.wicket.application.IComponentOnBeforeRenderListener#onBeforeRender(org.apache.wicket.Component)
		 */
		@SuppressWarnings("unchecked")
		public void onAfterRender(final Component component)
		{
			if (log.isWarnEnabled())
			{
				if ((component instanceof FormComponent) ||
					(component instanceof IFormSubmittingComponent) || (component instanceof Form))
				{
					log.warn("Please note that onBeforeRender() and validate() might be called on invisible components inside an Enclosure. " +
						"Please see EnclosureContainer for an alternative. Enclosure: " +
						enclosure.toString());
				}
			}
		}
	}
}
