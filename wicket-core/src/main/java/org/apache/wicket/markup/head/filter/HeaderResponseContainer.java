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
package org.apache.wicket.markup.head.filter;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.parser.XmlTag.TagType;

/**
 * A container that renders the content that was bucketed into a certain bucket by
 * {@link FilteringHeaderResponse}.
 * 
 * Note that this container renders only its body by default.
 * 
 * @author Jeremy Thomerson
 */
public class HeaderResponseContainer extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	private final String filterName;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the wicket id for this container
	 * @param filterName
	 *            the name of the filter that is bucketing stuff to be rendered in this container
	 */
	public HeaderResponseContainer(String id, String filterName)
	{
		super(id);
		this.filterName = filterName;
		setRenderBodyOnly(true);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		// force this into an open-close tag rather than a self-closing tag
		tag.setType(TagType.OPEN);
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		FilteringHeaderResponse response = FilteringHeaderResponse.get();
		if (!response.isClosed())
		{
			throw new RuntimeException(
				"there was an error processing the header response - you tried to render a bucket of response from FilteringHeaderResponse, but it had not yet run and been closed.  this should occur when the header container that is standard in wicket renders, so perhaps you have done something to keep that from rendering?");
		}
		CharSequence foot = response.getContent(filterName);
		replaceComponentTagBody(markupStream, openTag, foot);
	}
}
