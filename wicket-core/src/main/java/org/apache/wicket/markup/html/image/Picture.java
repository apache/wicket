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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * A component which displays localizable image resources within source and image elements as
 * responsive image. Elements are added with addImage / addSource.
 * 
 * @author Tobias Soloschenko
 */
public class Picture extends WebMarkupContainer
{

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a picture component
	 * 
	 * @param id
	 *            the id of the picture component
	 */
	public Picture(String id)
	{
		super(id);
	}

	/**
	 * Creates a picture component
	 * 
	 * @param id
	 *            the id of the picture component
	 * @param model
	 *            the component's model
	 */
	public Picture(String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * builds the component tag and checks the tag to be a picture
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		this.checkComponentTag(tag, "picture");
		super.onComponentTag(tag);
	}

	/**
	 * Adds an image
	 * 
	 * @param image
	 *            the image to add
	 */
	public void addImage(Image image)
	{
		this.add(image);
	}

	/**
	 * Adds a source
	 * 
	 * @param source
	 *            the source to add
	 */
	public void addSource(Source source)
	{
		this.add(source);
	}
}
