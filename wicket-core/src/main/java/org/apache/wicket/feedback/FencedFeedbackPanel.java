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
package org.apache.wicket.feedback;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

/**
 * A specialized feedback panel that only displays messages from inside a fence defined by a
 * container component. Instances will not show messages coming from inside a nested fence, allowing
 * the nesting of these panels to work correctly without displaying the same feedback message twice.
 * A constructor that does not takes a fencing component creates a catch-all panel that shows
 * messages that do not come from inside any fence or from the {@link Session}.
 * 
 * <h2>IN DEPTH EXPLANATION</h2>
 * <p>
 * It is often very useful to have feedback panels that show feedback that comes from inside a
 * certain container only. For example given a page with the following structure:
 * </p>
 * 
 * <pre>
 * Page
 *   Form1
 *     Feedback1
 *     Input1
 *     Form2
 *       Feedback2
 *       Input2
 * </pre>
 * <p>
 * we want Feedback2 to show messages originating only from inside Form2 and Feedback1 to show
 * messages only originating from Form1 but not Form2 (because messages originating from Form2 are
 * already shown by Feedback2).
 * </p>
 * <p>
 * It is fairly simple to configure Feedback2 - a {@link ContainerFeedbackMessageFilter} added to
 * the regular {@link FeedbackPanel} will do the trick. The hard part is configuring Feedback1. We
 * can add a {@link ContainerFeedbackMessageFilter} to it, but since Form2 is inside Form1 the
 * container filter will allow messages from both Form1 and Form2 to be added to FeedbackPanel1.
 * </p>
 * <p>
 * This is where the {@link FencedFeedbackPanel} comes in. All we have to do is to make
 * FeedbackPanel2 a {@link FencedFeedbackPanel} with the fencing component defined as Form2 and
 * Feedback1 a {@link FencedFeedbackPanel} with the fencing component defiend as Form1.
 * {@link FencedFeedbackPanel} will only show messages that original from inside its fencing
 * component and not from inside any descendant component that acts as a fence for another
 * {@link FencedFeedbackPanel}.
 * </p>
 * <p>
 * When created with a {@code null} fencing component or using a constructor that does not take one
 * the panel will only display messages that do not come from inside a fence. It will also display
 * messages that come from {@link Session}. This acts as a catch-all panels showing messages that
 * would not be shown using any other instance of the {@link FencedFeedbackPanel} created witha
 * fencing component. There is usually one instance of such a panel at the top of the page to
 * display notifications of success.
 * </p>
 * 
 * @author igor
 */
public class FencedFeedbackPanel extends FeedbackPanel
{
	private static final long serialVersionUID = 1L;

	private static MetaDataKey<Integer> FENCE_KEY = new MetaDataKey<Integer>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final Component fence;

	/**
	 * Creates a catch-all feedback panel that will show messages not coming from any fence,
	 * including messages coming from {@link Session}
	 * 
	 * @param id
	 */
	public FencedFeedbackPanel(String id)
	{
		this(id, (Component)null);
	}

	/**
	 * Creates a feedback panel that will only show messages if they original from, or inside of,
	 * the {@code fence} component and not from any inner fence.
	 * 
	 * @param id
	 * @param fence
	 */
	public FencedFeedbackPanel(String id, Component fence)
	{
		this(id, fence, null);
	}

	/**
	 * Creates a catch-all instance witha filter.
	 * 
	 * @see #FencedFeedbackPanel(String)
	 * 
	 * @param id
	 * @param filter
	 */
	public FencedFeedbackPanel(String id, IFeedbackMessageFilter filter)
	{
		this(id, null, filter);
	}

	/**
	 * Creates a fenced feedback panel with a filter.
	 * 
	 * @see #FencedFeedbackPanel(String, Component)
	 * 
	 * @param id
	 * @param fence
	 * @param filter
	 */
	public FencedFeedbackPanel(String id, Component fence, IFeedbackMessageFilter filter)
	{
		super(id, filter);
		this.fence = fence;
		if (fence != null)
		{
			Integer count = fence.getMetaData(FENCE_KEY);
			count = count == null ? 1 : count + 1;
			fence.setMetaData(FENCE_KEY, count);
		}
	}

	@Override
	protected void onRemove()
	{
		super.onRemove();
		if (fence != null)
		{
			// decrement the fence count

			Integer count = fence.getMetaData(FENCE_KEY);
			count = count == 1 ? null : count - 1;
			fence.setMetaData(FENCE_KEY, count);
		}
	}

	@Override
	protected FeedbackMessagesModel newFeedbackMessagesModel()
	{
		return new FeedbackMessagesModel(this)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected List<FeedbackMessage> collectMessages(Component panel,
				IFeedbackMessageFilter filter)
			{
				if (fence == null)
				{
					// this is the catch-all panel

					return new FeedbackCollector(panel.getPage())
					{
						@Override
						protected boolean shouldRecurseInto(Component component)
						{
							return component.getMetaData(FENCE_KEY) == null;
						}
					}.collect(filter);
				}
				else
				{
					// this is a fenced panel

					return new FeedbackCollector(fence)
					{
						@Override
						protected boolean shouldRecurseInto(Component component)
						{
							// only recurse into components that are not fences

							return component.getMetaData(FENCE_KEY) == null;
						}
					}.setIncludeSession(false).collect(filter);
				}
			}
		};
	}
}
