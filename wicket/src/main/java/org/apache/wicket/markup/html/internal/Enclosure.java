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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.border.Border.BorderBodyContainer;
import org.apache.wicket.markup.parser.filter.EnclosureHandler;
import org.apache.wicket.markup.resolver.EnclosureResolver;
import org.apache.wicket.util.collections.ReadOnlyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An Enclosure are automatically created by Wicket. Do not create it yourself. An Enclosure
 * container is created if &lt;wicket:enclosure&gt; is found in the markup. It is meant to solve the
 * following situation. Instead of
 * 
 * <pre>
 *    &lt;table wicket:id=&quot;label-container&quot; class=&quot;notify&quot;&gt;&lt;tr&gt;&lt;td&gt;&lt;span wicket:id=&quot;label&quot;&gt;[[notification]]&lt;/span&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt; 
 *  
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
 * 
 *    add(new Label(&quot;label&quot;, notificationModel)) 
 *    {
 *       public boolean isVisible() 
 *       {
 *           return hasNotification();
 *       }
 *    }
 * </pre>
 * 
 * @see EnclosureResolver
 * @see EnclosureHandler
 * 
 * @author Juergen Donnerstag
 * @since 1.3
 */
public class Enclosure extends WebMarkupContainer
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
		this.childId = childId;
	}

	/**
	 * 
	 * @see org.apache.wicket.MarkupContainer#isTransparentResolver()
	 */
	public boolean isTransparentResolver()
	{
		return true;
	}

	/**
	 * 
	 * @param childId
	 * @return Child Component
	 */
	public Component getChildComponent()
	{
		if (childComponent == null)
		{
			MarkupContainer parent = getEnclosureParent();

			if (childId == null)
			{
				throw new MarkupException(
					"You most likely forgot to register the EnclosureHandler with the MarkupParserFactory");
			}

			final Component child = parent.get(childId.toString());
			if (child == null)
			{
				throw new MarkupException(
					"Didn't find child component of <wicket:enclosure> with id='" + childId +
						"'. Component: " + this.toString());
			}
			childComponent = child;
		}
		return childComponent;
	}

	/**
	 * Get the real parent container
	 * 
	 * @return
	 */
	private MarkupContainer getEnclosureParent()
	{
		MarkupContainer parent = getParent();
		while (parent != null)
		{
			if (parent.isTransparentResolver())
			{
				parent = parent.getParent();
			}
			else if (parent instanceof BorderBodyContainer)
			{
				parent = ((BorderBodyContainer)parent).findParent(Border.class);
			}
			else
			{
				break;
			}
		}

		if (parent == null)
		{
			throw new WicketRuntimeException(
				"Unable to find parent component which is not a transparent resolver");
		}
		return parent;
	}

	/**
	 * 
	 * @see org.apache.wicket.MarkupContainer#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		final Component controller = getChildComponent();
		if (controller == this)
		{
			throw new WicketRuntimeException(
				"Programming error: childComponent == enclose component; endless loop");
		}

		setVisible(controller.determineVisibility());

		// transfer visibility to direct children
		DirectChildTagIterator it = new DirectChildTagIterator(markupStream, openTag);
		MarkupContainer controllerParent = getEnclosureParent();
		while (it.hasNext())
		{
			ComponentTag t = (ComponentTag)it.next();
			Component child = controllerParent.get(t.getId());
			if (child != null)
			{
				child.setVisibilityAllowed(isVisible());
			}
		}
		it.rewind();

		if (isVisible() == true)
		{
			super.onComponentTagBody(markupStream, openTag);
		}
		else
		{
			markupStream.skipToMatchingCloseTag(openTag);
		}
	}

	/**
	 * Iterator that iterates over direct child component tags of the given component tag
	 * 
	 */
	private static class DirectChildTagIterator extends ReadOnlyIterator
	{
		private final MarkupStream markupStream;
		private final ComponentTag parent;
		private ComponentTag next = null;
		private final int originalIndex;

		/**
		 * Construct.
		 * 
		 * @param markupStream
		 * @param parent
		 */
		public DirectChildTagIterator(MarkupStream markupStream, ComponentTag parent)
		{
			super();
			this.markupStream = markupStream;
			this.parent = parent;
			originalIndex = markupStream.getCurrentIndex();
			findNext();
		}

		/**
		 * Resets markup stream to the original position
		 */
		public void rewind()
		{
			markupStream.setCurrentIndex(originalIndex);
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext()
		{
			return next != null;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next()
		{
			ComponentTag ret = next;
			findNext();
			return ret;
		}

		private void findNext()
		{
			ComponentTag tag = next;
			next = null;

			if (tag != null && tag.isOpenClose())
			{
				// if current child tag is open-close look for next child
				tag = null;
			}

			while (markupStream.hasMore())
			{
				final MarkupElement cursor = markupStream.next();

				if (cursor.closes(parent))
				{
					// parent close tag found, we are done
					break;
				}

				if (tag != null && cursor.closes(tag))
				{
					// child tag is closed, next tag is either parent-close or next direct child
					tag = null;
				}
				else if (tag == null && cursor instanceof ComponentTag)
				{
					// found next child
					next = (ComponentTag)cursor;
					break;
				}
			}
		}
	}
}
