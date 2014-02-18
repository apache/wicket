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
package org.apache.wicket.markup.html.border;

import org.apache.wicket.Component;
import org.apache.wicket.IQueueRegion;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupFragment;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.TagUtils;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.BorderMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * A border component has associated markup which is drawn and determines placement of markup and/or
 * components nested within the border component.
 * <p>
 * The portion of the border's associated markup file which is to be used in rendering the border is
 * denoted by a &lt;wicket:border&gt; tag. The children of the border component instance are then
 * inserted into this markup, replacing the first &lt;wicket:body&gt; tag in the border's associated
 * markup.
 * <p>
 * For example, if a border's associated markup looked like this:
 * 
 * <pre>
 *   &lt;html&gt;
 *   &lt;body&gt;
 *     &lt;wicket:border&gt;
 *       First &lt;wicket:body/&gt; Last
 *     &lt;/wicket:border&gt;
 *   &lt;/body&gt;
 *   &lt;/html&gt;
 * </pre>
 * 
 * And the border was used on a page like this:
 * 
 * <pre>
 *   &lt;html&gt;
 *   &lt;body&gt;
 *     &lt;span wicket:id = &quot;myBorder&quot;&gt;
 *       Middle
 *     &lt;/span&gt;
 *   &lt;/body&gt;
 *   &lt;/html&gt;
 * </pre>
 * 
 * Then the resulting HTML would look like this:
 * 
 * <pre>
 *   &lt;html&gt;
 *   &lt;body&gt;
 *     First Middle Last
 *   &lt;/body&gt;
 *   &lt;/html&gt;
 * </pre>
 * 
 * In other words, the body of the myBorder component is substituted into the border's associated
 * markup at the position indicated by the &lt;wicket:body&gt; tag.
 * <p>
 * Regarding &lt;wicket:body/&gt; you have two options. Either use &lt;wicket:body/&gt; (open-close
 * tag) which will automatically be expanded to &lt;wicket:body&gt;body content&lt;/wicket:body&gt;
 * or use &lt;wicket:body&gt;preview region&lt;/wicket:body&gt; in your border's markup. The preview
 * region (everything in between the open and close tag) will automatically be removed.
 * <p>
 * The border body container will automatically be created for you and added to the border
 * container. It is accessible via {@link #getBodyContainer()}. In case the body markup is not an
 * immediate child of border (see the example below), then you must use code such as
 * <code>someContainer.add(getBodyContainer())</code> to add the body component to the correct
 * container.
 * 
 * <pre>
 *   &lt;html&gt;
 *   &lt;body&gt;
 *     &lt;wicket:border&gt;
 *       &lt;span wicket:id=&quot;someContainer&quot;&gt;
 *         &lt;wicket:body/&gt;
 *       &lt;/span&gt;
 *     &lt;/wicket:border&gt;
 *   &lt;/body&gt;
 *   &lt;/html&gt;
 * </pre>
 * 
 * The component "someContainer" in the previous example must be added to the border, and not the
 * body, which is achieved via {@link #addToBorder(Component...)}.
 * <p/>
 * {@link #add(Component...)} is an alias to {@code getBodyContainer().add(Component...)} and will
 * add a child component to the border body as shown in the example below.
 * 
 * <pre>
 *   &lt;html&gt;
 *   &lt;body&gt;
 *     &lt;span wicket:id = &quot;myBorder&quot;&gt;
 *       &lt;input wicket:id=&quot;name&quot/;&gt;
 *     &lt;/span&gt;
 *   &lt;/body&gt;
 *   &lt;/html&gt;
 * </pre>
 * 
 * This implementation does not apply any magic with respect to component handling. In doubt think
 * simple. But everything you can do with a MarkupContainer or Component, you can do with a Border
 * or its Body as well.
 * <p/>
 * 
 * Other methods like {@link #remove()}, {@link #get(int)}, {@link #iterator()}, etc. are not
 * aliased to work on the border's body and attention must be paid when they need to be used.
 * 
 * @see BorderPanel An alternative implementation based on Panel
 * @see BorderBehavior A behavior which adds (raw) markup before and after the component
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public abstract class Border extends WebMarkupContainer implements IComponentResolver, IQueueRegion
{
	private static final long serialVersionUID = 1L;

	/** */
	public static final String BODY = "body";

	/** */
	public static final String BORDER = "border";

	/** The body component associated with <wicket:body> */
	private final BorderBodyContainer body;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public Border(final String id)
	{
		this(id, null);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Border(final String id, final IModel<?> model)
	{
		super(id, model);

		body = new BorderBodyContainer(id + "_" + BODY);
		queueToBorder(body);
	}

	/**
	 * @return The border body container
	 */
	public final BorderBodyContainer getBodyContainer()
	{
		return body;
	}

	/**
	 * This is for all components which have been added to the markup like this:
	 * 
	 * <pre>
	 * 	&lt;span wicket:id="myBorder"&gt;
	 * 		&lt;input wicket:id="text1" .. /&gt;
	 * 		...
	 * 	&lt;/span&gt;
	 * 
	 * </pre>
	 * 
	 * Whereas {@link #addToBorder(Component...)} will add a component associated with the following
	 * markup:
	 * 
	 * <pre>
	 * 	&lt;wicket:border&gt;
	 * 		&lt;form wicket:id="myForm" .. &gt;
	 * 			&lt;wicket:body/&gt;
	 * 		&lt;/form&gt;
	 * 	&lt;/wicket:border&gt;
	 * 
	 * </pre>
	 * 
	 * @see org.apache.wicket.MarkupContainer#add(org.apache.wicket.Component[])
	 */
	@Override
	public Border add(final Component... children)
	{
		getBodyContainer().add(children);
		return this;
	}

	@Override
	public Border addOrReplace(final Component... children)
	{
		getBodyContainer().addOrReplace(children);
		return this;
	}

	@Override
	public Border queue(Component... components)
	{
		getBodyContainer().queue(components);
		return this;
	}

	@Override
	public Border remove(final Component component)
	{
		if (component == body)
		{
			// when the user calls foo.add(getBodyContainer()) this method will be called with it to
			// clear body container's old parent, in which case we do not want to redirect to body
			// container but to border's old remove.
			super.remove(body);
		}
		else
		{
			getBodyContainer().remove(component);
		}
		return this;
	}

	@Override
	public Border remove(final String id)
	{
		getBodyContainer().remove(id);
		return this;
	}

	@Override
	public Border removeAll()
	{
		getBodyContainer().removeAll();
		return this;
	}

	@Override
	public Border replace(final Component replacement)
	{
		getBodyContainer().replace(replacement);
		return this;
	}

	/**
	 * Adds children components to the Border itself
	 * 
	 * @param children
	 *            the children components to add
	 * @return this
	 */
	public Border addToBorder(final Component... children)
	{
		super.add(children);
		return this;
	}

	/**
	 * Queues children components to the Border itself
	 *
	 * @param children
	 *            the children components to queue
	 * @return this
	 */
	public Border queueToBorder(final Component... children)
	{
		super.queue(children);
		return this;
	}

	/**
	 * Removes child from the Border itself
	 * 
	 * @param child
	 * @return {@code this}
	 */
	public Border removeFromBorder(final Component child)
	{
		super.remove(child);
		return this;
	}

	/**
	 * Replaces component in the Border itself
	 * 
	 * @param component
	 * @return {@code this}
	 */
	public Border replaceInBorder(final Component component)
	{
		super.replace(component);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		// make sure nested borders are resolved properly
		if (body.rendering == false)
		{
			// We are only interested in border body tags. The tag ID actually is irrelevant since
			// always preset with the same default
			if (TagUtils.isWicketBodyTag(tag))
			{
				return body;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IMarkupSourcingStrategy newMarkupSourcingStrategy()
	{
		return new BorderMarkupSourcingStrategy();
	}

	/**
	 * Search for the child markup in the file associated with the Border. The child markup must in
	 * between the &lt;wicket:border&gt; tags.
	 */
	@Override
	public IMarkupFragment getMarkup(final Component child)
	{
		// Border require an associated markup resource file
		IMarkupFragment markup = getAssociatedMarkup();
		if (markup == null)
		{
			throw new MarkupException("Unable to find associated markup file for Border: " +
				this.toString());
		}

		// Find <wicket:border>
		IMarkupFragment borderMarkup = null;
		for (int i = 0; i < markup.size(); i++)
		{
			MarkupElement elem = markup.get(i);
			if (TagUtils.isWicketBorderTag(elem))
			{
				borderMarkup = new MarkupFragment(markup, i);
				break;
			}
		}

		if (borderMarkup == null)
		{
			throw new MarkupException(markup.getMarkupResourceStream(),
				"Unable to find <wicket:border> tag in associated markup file for Border: " +
					this.toString());
		}

		// If child == null, return the markup fragment starting with the <wicket:border> tag
		if (child == null)
		{
			return borderMarkup;
		}

		// Is child == BorderBody?
		if (child == body)
		{
			// Get the <wicket:body> markup
			return body.getMarkup();
		}

		// Find the markup for the child component
		IMarkupFragment childMarkup = borderMarkup.find(child.getId());
		if (childMarkup != null)
		{
			return childMarkup;
		}

		return ((BorderMarkupSourcingStrategy)getMarkupSourcingStrategy()).findMarkupInAssociatedFileHeader(
			this, child);
	}

	/**
	 * The container to be associated with the &lt;wicket:body&gt; tag
	 */
	public class BorderBodyContainer extends WebMarkupContainer implements IQueueRegion
	{
		private static final long serialVersionUID = 1L;

		/** The markup */
		private transient IMarkupFragment markup;

		// properly resolve borders added to borders
		protected boolean rendering;

		/**
		 * Constructor
		 * 
		 * @param id
		 */
		public BorderBodyContainer(final String id)
		{
			super(id);
		}

		@Override
		protected void onComponentTag(final ComponentTag tag)
		{
			// Convert open-close to open-body-close
			if (tag.isOpenClose())
			{
				tag.setType(TagType.OPEN);
				tag.setModified(true);
			}

			super.onComponentTag(tag);
		}

		@Override
		public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
		{
			// skip the <wicket:body> body
			if (markupStream.getPreviousTag().isOpen())
			{
				// Only RawMarkup is allowed within the preview region,
				// which gets stripped from output
				markupStream.skipRawMarkup();
			}

			// Get the <span wicket:id="myBorder"> markup and render that instead
			IMarkupFragment markup = Border.this.getMarkup();
			MarkupStream stream = new MarkupStream(markup);
			ComponentTag tag = stream.getTag();
			stream.next();

			super.onComponentTagBody(stream, tag);
		}

		@Override
		protected void onRender()
		{
			rendering = true;

			try
			{
				super.onRender();
			}
			finally
			{
				rendering = false;
			}
		}

		/**
		 * Get the &lt;wicket:body&gt; markup from the body's parent container
		 */
		@Override
		public IMarkupFragment getMarkup()
		{
			if (markup == null)
			{
				markup = findByName(getParent().getMarkup(null), BODY);
			}
			return markup;
		}

		/**
		 * Search for &lt;wicket:'name' ...&gt; on the same level, but ignoring other "transparent"
		 * tags such as &lt;wicket:enclosure&gt; etc.
		 * 
		 * @param markup
		 * @param name
		 * @return null, if not found
		 */
		private final IMarkupFragment findByName(final IMarkupFragment markup, final String name)
		{
			Args.notEmpty(name, "name");

			MarkupStream stream = new MarkupStream(markup);

			// Skip any raw markup
			stream.skipUntil(ComponentTag.class);

			// Skip <wicket:border>
			stream.next();

			while (stream.skipUntil(ComponentTag.class))
			{
				ComponentTag tag = stream.getTag();
				if (tag.isOpen() || tag.isOpenClose())
				{
					if (TagUtils.isWicketBodyTag(tag))
					{
						return stream.getMarkupFragment();
					}
				}

				stream.next();
			}

			return null;
		}

		/**
		 * Get the child markup which must be in between the &lt;span wicktet:id="myBorder"&gt; tags
		 */
		@Override
		public IMarkupFragment getMarkup(final Component child)
		{
			IMarkupFragment markup = Border.this.getMarkup();
			if (markup == null)
			{
				return null;
			}

			if (child == null)
			{
				return markup;
			}

			return markup.find(child.getId());
		}

		@Override
		public IMarkupFragment getDequeueMarkup()
		{
			Border border = findParent(Border.class);
			IMarkupFragment fragment = border.getMarkup();
			/*
			 * we want to get the contents of the border here (the markup that
			 * is represented by the body tag) to do this we need to strip the
			 * tag that the border is attached to (usually the first tag)
			 */

			int i = 0;
			while (i < fragment.size())
			{
				// TODO queueing Use fragment.find(border.getId()); instead ?!
				MarkupElement element = fragment.get(i);
				if (element instanceof ComponentTag
						&& ((ComponentTag)element).getId().equals(border.getId()))
				{
					break;
				}
				i++;
			}

			if (i >= fragment.size())
			{
				throw new IllegalStateException("Could not find starting border tag for border: "
						+ border.getId() + " in markup: " + fragment);
			}


			/* TODO queueing The comment is not finished
			 * (i) is now at the border tag, find the next component tag which
			 */

			i++;
			while (i < fragment.size())
			{
				MarkupElement element = fragment.get(i);
				if (element instanceof ComponentTag)
				{
					break;
				}
				i++;
			}

			ComponentTag tag = (ComponentTag)fragment.get(i);
			if (tag.isClose())
			{
				// this closes the border tag, border only has raw markup
				return null;
			}

			return new MarkupFragment(fragment, i);
		}

		@Override
		public Component findComponentToDequeue(ComponentTag tag)
		{
			/*
			 * the body container is allowed to search for queued components all
			 * the way to the page even though it is an IQueueRegion so it can
			 * find components queued below the border
			 */

			Component component = super.findComponentToDequeue(tag);
			if (component != null)
			{
				return component;
			}

			MarkupContainer cursor = getParent();
			while (cursor != null)
			{
				component = cursor.findComponentToDequeue(tag);
				if (component != null)
				{
					return component;
				}
				if (cursor instanceof BorderBodyContainer)
				{
					// optimization - find call above would've already recursed
					// to page
					break;
				}
				cursor = cursor.getParent();
			}
			return null;
		}
	}
	
	
	@Override
	protected boolean canDequeueTag(ComponentTag tag)
	{
		if ((tag instanceof WicketTag)&&((WicketTag)tag).isBodyTag())
		{
			return true;
		}

		return super.canDequeueTag(tag);
	}
	
	@Override
	public Component findComponentToDequeue(ComponentTag tag)
	{
		if ((tag instanceof WicketTag) && ((WicketTag)tag).isBodyTag())
		{
			return getBodyContainer();
		}
		return super.findComponentToDequeue(tag);
	}
	
	@Override
	protected void addDequeuedComponent(Component component, ComponentTag tag)
	{
		// components queued in border get dequeued into the border not into the body container
		addToBorder(component);
	}
}
