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
package org.apache.wicket.examples.base.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Renders an anchor tag with a link to an identified element in the document, and replaces its body
 * with the model value if a model was set. Handy for in-document navigation (in-line menus).
 */
public class AnchorLink extends WebComponent
{
	private static final long serialVersionUID = 1L;
	private final String anchor;

	/**
	 * Constructs an {@link AnchorLink} that doesn't replace its body.
	 * 
	 * @param id
	 *            the component identifier
	 * @param anchor
	 *            the anchor to link to
	 */
	public AnchorLink(String id, String anchor)
	{
		this(id, Model.of(), anchor);
	}

	/**
	 * Constructs an {@link AnchorLink} that replaces its body with the {@code contents}.
	 * 
	 * @param id
	 *            the component identifier
	 * @param contents
	 *            the contents that replace the body of this link
	 * @param anchor
	 *            the anchor to link to
	 */
	public AnchorLink(String id, String contents, String anchor)
	{
		this(id, Model.of(contents), anchor);
	}

	/**
	 * Constructs an {@link AnchorLink} that replaces its body with the {@code contents}.
	 * 
	 * @param id
	 *            the component identifier
	 * @param contents
	 *            the contents that replace the body of this link
	 * @param anchor
	 *            the anchor to link to
	 */
	public AnchorLink(String id, IModel<?> contents, String anchor)
	{
		super(id, contents);
		this.anchor = anchor;
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("href", "#" + anchor);
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		if (getDefaultModel() != null && getDefaultModel().getObject() != null)
		{
			replaceComponentTagBody(markupStream, openTag, getDefaultModelObjectAsString());
		}
	}
}
