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
package wicket.markup.parser.filter;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.model.Model;


/**
 * Mock page for testing.
 *
 * @author Chris Turner
 */
public class HeaderSectionMyLabel extends WebMarkupContainerWithAssociatedMarkup
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * @param id
	 * @param label
	 */
	public HeaderSectionMyLabel(final String id, final String label) 
	{
	    super(id, new Model(label));
    }

	/**
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		replaceComponentTagBody(markupStream, openTag, getModelObjectAsString());
	}
	
    /**
     * This label renders its markup the normal way, and is still able
     * to take and render the header tag from an associated markup file.
     * 
     * @see wicket.Component#renderHead(wicket.markup.html.internal.HtmlHeaderContainer)
     */
    public void renderHead(HtmlHeaderContainer container)
    {
    	this.renderHeadFromAssociatedMarkupFile(container);
    	super.renderHead(container);
    }
}
