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
package org.apache.wicket.markup.html.panel;

import org.apache.wicket.Component;
import org.apache.wicket.DequeueContext;
import org.apache.wicket.IQueueRegion;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;

/**
 * Usually you either have a markup file or a xml tag with wicket:id="myComponent" to associate
 * markup with a component. However in some use cases, especially when working with small panels it
 * is a bit awkward to maintain tiny pieces of markup in plenty of panel markup files. Use cases are
 * for example list views where list items are different depending on a state.
 * <p>
 * Fragments provide a means to maintain the panels tiny piece of markup. Since it can be anywhere,
 * the component whose markup contains the fragment's markup must be provided (markup provider).
 * <p>
 * 
 * <pre>
 *  &lt;span wicket:id=&quot;myPanel&quot;&gt;Example input (will be removed)&lt;/span&gt;
 * 
 *  &lt;wicket:fragment wicket:id=&quot;frag1&quot;&gt;panel 1&lt;/wicket:fragment&gt;
 *  &lt;wicket:fragment wicket:id=&quot;frag2&quot;&gt;panel 2&lt;/wicket:fragment&gt;
 * </pre>
 * 
 * <pre>
 *  add(new Fragment(&quot;myPanel1&quot;, &quot;frag1&quot;, myPage);
 * </pre>
 * 
 * @author Juergen Donnerstag
 */
public class Fragment extends WebMarkupContainer implements IQueueRegion
{
	private static final long serialVersionUID = 1L;

	/** The wicket:id of the associated markup fragment */
	private final String associatedMarkupId;

	private final MarkupContainer markupProvider;

	/**
	 * Constructor.
	 * 
	 * @see org.apache.wicket.Component#Component(String)
	 * 
	 * @param id
	 *            The component id
	 * @param markupId
	 *            The associated id of the associated markup fragment
	 * @param markupProvider
	 *            The component whose markup contains the fragment's markup
	 */
	public Fragment(final String id, final String markupId, final MarkupContainer markupProvider)
	{
		this(id, markupId, markupProvider, null);
	}

	/**
	 * Constructor.
	 * 
	 * @see org.apache.wicket.Component#Component(String)
	 * 
	 * @param id
	 *            The component id
	 * @param markupId
	 *            The associated id of the associated markup fragment
	 * @param markupProvider
	 *            The component whose markup contains the fragment's markup
	 * @param model
	 *            The model for this fragment
	 */
	public Fragment(final String id, final String markupId, final MarkupContainer markupProvider,
		final IModel<?> model)
	{
		super(id, model);

		associatedMarkupId = Args.notNull(markupId, "markupId");
		this.markupProvider = markupProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IMarkupSourcingStrategy newMarkupSourcingStrategy()
	{
		return new FragmentMarkupSourcingStrategy(associatedMarkupId, markupProvider)
		{
			@Override
			public IMarkupFragment chooseMarkup(Component component)
			{
				return Fragment.this.chooseMarkup(getMarkupProvider(component));
			}
		};
	}

	/**
	 * Get the markup stream which shall be used to search for the fragment
	 * 
	 * @param provider
	 * @return The markup stream to be used to find the fragment markup
	 */
	protected IMarkupFragment chooseMarkup(final MarkupContainer provider)
	{
		return provider.getMarkup(null);
	}

	/**
	 * @return the markup id associated to this Fragment
	 */
	public final String getAssociatedMarkupId()
	{
		return associatedMarkupId;
	}


	@Override
	public DequeueContext newDequeueContext()
	{
		IMarkupFragment markup = getMarkupSourcingStrategy().getMarkup(this, null);
		if (markup == null)
		{
			return null;
		}

		return new DequeueContext(markup, this, true);
	}
}
