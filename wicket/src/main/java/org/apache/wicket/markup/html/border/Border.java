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
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupFragment;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.resolver.BorderBodyResolver;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.IModel;

/**
 * A border component has associated markup which is drawn and determines placement of any markup
 * and/or components nested within the border component.
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
 * Note that if body is not an immediate child of border (example see below), than you must use code
 * like the following <code>someContainer.add(getBodyContainer())</code> to add the body component
 * to the correct container.
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
 * Please note that rather sooner than later Border will be removed and BaseBorder be renamed to
 * Border.
 * 
 * @see BorderBodyResolver
 * @see BorderBodyContainer
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public abstract class Border extends BaseBorder implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/** Should be true for bordered pages */
	private boolean transparentResolver = false;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public Border(final String id)
	{
		super(id);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Border(final String id, final IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * @see org.apache.wicket.markup.html.border.BaseBorder#newBorderBodyContainer(java.lang.String)
	 */
	@Override
	MarkupContainer newBorderBodyContainer(String id)
	{
		return new BorderBodyContainer(BODY_ID);
	}

	/**
	 * Borders used for bordered pages should set it to "true". Default is "false". If enabled, than
	 * requests to find a component are passed to the parent container as well. Thus the child may
	 * not be added to the Border, but might be added to the parent of the Border as well.
	 * 
	 * @param enable
	 *            true, to enable transparent resolving
	 * @return this for chaining
	 * @deprecated in 1.5. Use {@link #getBodyContainer()} to create a proper hierarchy
	 */
	@Deprecated
	public final Border setTransparentResolver(final boolean enable)
	{
		transparentResolver = enable;
		return this;
	}

	/**
	 * @see #setTransparentResolver(boolean)
	 * @see org.apache.wicket.MarkupContainer#isTransparentResolver()
	 * @deprecated in 1.5. Use {@link #getBodyContainer()} to create a proper hierarchy
	 */
	@Deprecated
	@Override
	public boolean isTransparentResolver()
	{
		return transparentResolver;
	}

	/**
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
	 *      org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		// In case of nested Borders, the outer border is no longer able to find
		// its body container easily. Thus we need to help resolve it.

		// The container is the body component. Get the Border component.
		MarkupContainer border = container.getParent();
		while ((border != null) && !(border instanceof Border))
		{
			border = border.getParent();
		}

		// Avoid recursions. It is the outer border which needs help to resolve
		// it. Not the inner border (this == border).
		if ((border == null) || (this == border))
		{
			return false;
		}

		// Ignore everything else except Border
		if (!(border instanceof Border))
		{
			return false;
		}

		// Determine if tag is a <wicket:body> tag
		if (!(tag instanceof WicketTag))
		{
			return false;
		}

		// And it must be <wicket:body>
		final WicketTag wtag = (WicketTag)tag;
		if (!wtag.isBodyTag())
		{
			return false;
		}

		getBodyContainer().render(markupStream);
		return true;
	}

	/**
	 * Determines whether or not the border body is visible.
	 * 
	 * @return true if body of the border is visible, false otherwise
	 */
	private boolean isBodyVisible()
	{
		// in order to determine this we have to visit all components between the border and the
		// body because border body can be embedded inside other containers.

		boolean bodyVisible = true;
		Component cursor = getBodyContainer();
		while (cursor != this && bodyVisible)
		{
			bodyVisible = cursor.determineVisibility();
			cursor = cursor.getParent();
		}
		return bodyVisible;
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#getMarkup(org.apache.wicket.Component)
	 */
	@Override
	public IMarkupFragment getMarkup(final Component child)
	{
		if ((child != null) && (BODY_ID.equals(child.getId()) == false))
		{
			// First check if markup can be found in between <span wicket:id="myborder">...</span>
			IMarkupFragment markup = getMarkup();
			if (markup == null)
			{
				return null;
			}

			markup = markup.find(null, child.getId(), 0);
			if (markup != null)
			{
				return markup;
			}
		}

		// Than check if markup can be found in between <wicket:border>...</wicket:border>
		IMarkupFragment markup = getAssociatedMarkup();
		if (markup == null)
		{
			throw new MarkupException("Unable to find associated markup file for Border: " +
				this.toString());
		}

		// Find <wicket:border>
		int i;
		for (i = 0; i < markup.size(); i++)
		{
			MarkupElement elem = markup.get(i);
			if (elem instanceof WicketTag)
			{
				WicketTag tag = (WicketTag)elem;
				if (tag.isBorderTag())
				{
					break;
				}
			}
		}

		// If child == null, return the markup fragment starting with the <wicket:border> tag
		if (child == null)
		{
			return new MarkupFragment(markup, i);
		}

		if (BODY_ID.equals(child.getId()))
		{
			// Find <wicket:body>
			int index = markup.findComponentIndex(null, child.getId(), i);
			if (index == -1)
			{
				throw new MarkupException("Unable to find <wicket:body> tag. Border: " +
					this.toString() + ". Associated Markup: " + markup.toString());
			}
			return new MarkupFragment(markup, index);
		}

		// Find the markup for the child component
		return markup.find(null, child.getId(), i);
	}

	/**
	 * The container to be associated with the &lt;wicket:body&gt; tag
	 */
	public class BorderBodyContainer extends BaseBorder.BorderBodyContainer
		implements
			IComponentResolver
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * 
		 * @param id
		 */
		public BorderBodyContainer(final String id)
		{
			super(id);
		}

		/**
		 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer,
		 *      org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
		 */
		public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
		{
			// Usually you add child components to Border instead of Body. Hence
			// we need to help Body to properly resolve the children.
			String id = tag.getId();
			if (!id.equals(BODY_ID))
			{
				Component component = Border.this.get(id);
				if (component != null)
				{
					component.render(markupStream);
					return true;
				}
			}

			return false;
		}
	}
}
