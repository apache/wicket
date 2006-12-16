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
package wicket.markup.html.panel;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
import wicket.markup.parser.XmlTag;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.model.IModel;

/**
 * A panel is a reusable component that holds markup and other components.
 * <p>
 * Whereas WebMarkupContainer is an inline container like
 * 
 * <pre>
 *      ...
 *      &lt;span wicket:id=&quot;xxx&quot;&gt;
 *        &lt;span wicket:id=&quot;mylabel&quot;&gt;My label&lt;/span&gt;
 *        ....
 *      &lt;/span&gt;
 *      ...
 * </pre>
 * 
 * a Panel has its own associated markup file and the container content is taken
 * from that file, like:
 * 
 * <pre>
 *      &lt;span wicket:id=&quot;mypanel&quot;/&gt;
 *     
 *      TestPanel.html
 *      &lt;wicket:panel&gt;
 *        &lt;span wicket:id=&quot;mylabel&quot;&gt;My label&lt;/span&gt;
 *        ....
 *      &lt;/wicket:panel&gt;
 * </pre>
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class Panel<T> extends WebMarkupContainerWithAssociatedMarkup<T>
{
	private static final long serialVersionUID = 1L;
	
	/** */
	public static final String PANEL = "panel";

	static
	{
		// register "wicket:panel"
		WicketTagIdentifier.registerWellKnownTagName(PANEL);
	}

	/** If if tag was an open-close tag */
	private boolean wasOpenCloseTag = false;

	/**
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public Panel(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public Panel(MarkupContainer parent, final String id, final IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * Get the child markup fragment with the 'id'
	 *  
	 * @param id
	 * @return MarkupFragment
	 */
	@Override
	public MarkupFragment getMarkupFragment(final String id)
	{
		// Find the tag in the associated markup
		MarkupFragment fragment = getAssociatedMarkup(true).getWicketFragment(PANEL, true)
				.getChildFragment(id, false);
		
		if (fragment != null)
		{
			return fragment;
		}
		
		// wicket:head must be searched for outside wicket:panel
		return getAssociatedMarkup(true).getChildFragment(id, true);
	}

	/**
	 * 
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		if (tag.isOpenClose())
		{
			this.wasOpenCloseTag = true;

			// Convert <span wicket:id="myPanel" /> into
			// <span wicket:id="myPanel">...</span>
			tag.setType(XmlTag.Type.OPEN);
		}
		super.onComponentTag(tag);
	}

	/**
	 * 
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		// Render the associated markup
		renderAssociatedMarkup(PANEL,
				"Markup for a panel component has to contain part '<wicket:panel>'");

		if (this.wasOpenCloseTag == false)
		{
			// Skip any raw markup in the body
			markupStream.skipRawMarkup();
		}
	}
}
