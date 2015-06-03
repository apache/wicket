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

import java.io.IOException;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.image.ImageUtil;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

/**
 * The inline image is used to embed the complete image content within a HTML document.
 *
 * @author Tobias Soloschenko
 * @since 6.20.0
 *
 */
public class InlineImage extends WebComponent
{

	private static final long serialVersionUID = 1L;

	private PackageResourceReference packageResourceReference;

	/**
	 * Creates an inline image
	 *
	 * @param id
	 *            the id
	 * @param packageResourceReference
	 *            the package resource reference of the image
	 */
	public InlineImage(String id, PackageResourceReference packageResourceReference)
	{
		this(id, null, packageResourceReference);
	}

	/**
	 * Creates an inline image
	 *
	 * @param id
	 *            the id
	 * @param model
	 *            the model of the inline image
	 * @param packageResourceReference
	 *            the package resource reference of the image
	 */
	public InlineImage(String id, IModel<?> model, PackageResourceReference packageResourceReference)
	{
		super(id, model);
		this.packageResourceReference = packageResourceReference;
	}

	/**
	 * Renders the complete image tag with the base64 encoded content.
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		checkComponentTag(tag, "img");

		try
		{
			tag.put("src", ImageUtil.createBase64EncodedImage(packageResourceReference, false));
		}
		catch (ResourceStreamNotFoundException e)
		{
			throw new WicketRuntimeException("Couldn't find the resource stream", e);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Error while reading the resource stream", e);
		}
	}
}
