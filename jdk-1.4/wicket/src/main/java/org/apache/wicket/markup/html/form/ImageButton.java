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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.IResourceListener;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.markup.html.image.resource.DefaultButtonImageResource;
import org.apache.wicket.markup.html.image.resource.LocalizedImageResource;

/**
 * &lt;input type="image"&gt; component - like {@link Button} only with an image.
 * <p>
 * For details of how ImageButtons load, generate and manage images, see
 * {@link LocalizedImageResource}.
 * 
 * @author Jonathan Locke
 */
public class ImageButton extends Button implements IResourceListener
{
	private static final long serialVersionUID = 1L;
	
	/** The image resource this image component references */
	private final LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public ImageButton(String id)
	{
		super(id);
	}

	/**
	 * Constructs an image button directly from an image resource.
	 * 
	 * @param id
	 *            See Component
	 * 
	 * @param imageResource
	 *            The image resource
	 */
	public ImageButton(final String id, final WebResource imageResource)
	{
		super(id);
		this.localizedImageResource.setResource(imageResource);
	}

	/**
	 * Constructs an image directly from an image resource.
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared image resource
	 */
	public ImageButton(final String id, final ResourceReference resourceReference)
	{
		super(id);
		localizedImageResource.setResourceReference(resourceReference);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param label
	 *            The button label
	 */
	public ImageButton(final String id, final String label)
	{
		this(id, new DefaultButtonImageResource(label));
	}

	/**
	 * @see org.apache.wicket.IResourceListener#onResourceRequested()
	 */
	public void onResourceRequested()
	{
		localizedImageResource.onResourceRequested();
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	protected final void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "image");
		super.onComponentTag(tag);
		localizedImageResource.setSrcAttribute(tag);
	}
	
	/**
	 * @see org.apache.wicket.markup.html.form.Button#getStatelessHint()
	 */
	protected boolean getStatelessHint()
	{
		return localizedImageResource.isStateless();
	}
}
