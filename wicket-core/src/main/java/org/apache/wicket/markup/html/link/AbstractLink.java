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
package org.apache.wicket.markup.html.link;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * Base class that that contains functionality for rendering disabled links.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractLink extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/** this link's label/body. */
	private IModel<?> bodyModel;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public AbstractLink(String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the link's model
	 */
	public AbstractLink(String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * Renders this link's body.
	 * 
	 * @param markupStream
	 *            the markup stream
	 * @param openTag
	 *            the open part of this tag
	 * @see org.apache.wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		// Get a copy of the body model from the getBody() method. This method could be overridden.
		IModel<?> tmpBodyModel = getBody();

		if ((tmpBodyModel != null) && (tmpBodyModel.getObject() != null))
		{
			replaceComponentTagBody(markupStream, openTag,
				getDefaultModelObjectAsString(tmpBodyModel.getObject()));
		}
		else
		{
			// Render the body of the link
			super.onComponentTagBody(markupStream, openTag);
		}
	}

	/**
	 * Alters the tag so that the link renders as disabled.
	 * 
	 * This method is meant to be called from {@link #onComponentTag(ComponentTag)} method of the
	 * derived class.
	 * 
	 * @param tag
	 */
	protected void disableLink(final ComponentTag tag)
	{
		// if the tag is a button or input
		if ("button".equalsIgnoreCase(tag.getName()) || "input".equalsIgnoreCase(tag.getName()))
		{
			tag.put("disabled", "disabled");
		}
		else
		{
			// Remove any href from the old link
			tag.remove("href");

			tag.remove("onclick");
		}
	}

	/**
	 * @return the link's body model
	 */
	public IModel<?> getBody()
	{
		return bodyModel;
	}

	/**
	 * Sets the link's body model
	 * 
	 * @param bodyModel
	 * @return <code>this</code> for method chaining
	 */
	public AbstractLink setBody(final IModel<?> bodyModel)
	{
		this.bodyModel = wrap(bodyModel);
		return this;
	}

	@Override
	protected void onDetach()
	{
		super.onDetach();

		if (bodyModel != null)
		{
			bodyModel.detach();
		}
	}
}
