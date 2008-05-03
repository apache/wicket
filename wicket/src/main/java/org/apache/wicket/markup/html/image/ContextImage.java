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
package org.apache.wicket.markup.html.image;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

/**
 * Provides a context-relative image.
 * <p>
 * Provide a String in this component's model which will be prefixed such that the image is relative
 * to the context root, no matter what URL the page the ContextImage is on is rendered at.
 * 
 * @author Alastair Maw
 */
public class ContextImage extends WebComponent<String>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see Component#Component(String)
	 */
	public ContextImage(String id)
	{
		super(id);
	}

	/**
	 * @see Component#Component(String, IModel)
	 */
	public ContextImage(String id, IModel<String> model)
	{
		super(id, model);
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "img");
		super.onComponentTag(tag);
		tag.put("src", getRequest().getRelativePathPrefixToContextRoot() + getModelObjectAsString());
	}
}
