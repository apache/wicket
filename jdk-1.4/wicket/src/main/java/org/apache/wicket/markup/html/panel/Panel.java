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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.model.IModel;

/**
 * A panel is a reusable component that holds markup and other components.
 * <p>
 * Whereas WebMarkupContainer is an inline container like
 * <pre>
 *  ...
 *  &lt;span wicket:id=&quot;xxx&quot;&gt;
 *    &lt;span wicket:id=&quot;mylabel&quot;&gt;My label&lt;/span&gt;
 *    ....
 *  &lt;/span&gt;
 *  ...
 * </pre>
 * a Panel has its own associated markup file and the container content is
 * taken from that file, like:
 * <pre>
 *  &lt;span wicket:id=&quot;mypanel&quot;/&gt;
 * 
 *  TestPanel.html
 *  &lt;wicket:panel&gt;
 *    &lt;span wicket:id=&quot;mylabel&quot;&gt;My label&lt;/span&gt;
 *    ....
 *  &lt;/wicket:panel&gt;
 * </pre>
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class Panel extends WebMarkupContainerWithAssociatedMarkup
{
	private static final long serialVersionUID = 1L;

	static
	{
		// register "wicket:fragement"
		WicketTagIdentifier.registerWellKnownTagName("panel");
	}
	
	/** If if tag was an open-close tag */
	private boolean wasOpenCloseTag = false;
	
	/**
     * @see org.apache.wicket.Component#Component(String)
     */
    public Panel(final String id)
    {
        super(id);
    }
    
    /**
     * @see org.apache.wicket.Component#Component(String, IModel)
     */
    public Panel(final String id, final IModel model)
    {
        super(id, model);
    }    

    /**
     * 
     * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
     */
    protected void onComponentTag(final ComponentTag tag)
    {
    	if (tag.isOpenClose())
    	{
    		this.wasOpenCloseTag = true;
    		
    		// Convert <span wicket:id="myPanel" /> into 
    		// <span wicket:id="myPanel">...</span>  
    		tag.setType(XmlTag.OPEN);
    	}
    	super.onComponentTag(tag);
    }

    /**
     * 
     * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
     */
    protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
    {
        // Render the associated markup
        renderAssociatedMarkup("panel",
                "Markup for a panel component has to contain part '<wicket:panel>'");

        if (this.wasOpenCloseTag == false)
        {
			// Skip any raw markup in the body
			markupStream.skipRawMarkup();
        }
    }

    /**
     * Check the associated markup file for a wicket header tag
     * 
     * @see org.apache.wicket.Component#renderHead(org.apache.wicket.markup.html.internal.HtmlHeaderContainer)
     */
    public void renderHead(HtmlHeaderContainer container)
    {
    	this.renderHeadFromAssociatedMarkupFile(container);
    	super.renderHead(container);
    }
}
