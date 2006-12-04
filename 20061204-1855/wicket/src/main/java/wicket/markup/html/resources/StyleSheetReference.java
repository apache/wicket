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
package wicket.markup.html.resources;

import wicket.ResourceReference;
import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.util.value.ValueMap;

/**
 * Link to a packaged style sheet.
 *
 * @author Eelco Hillenius
 */
public final class StyleSheetReference extends PackagedResourceReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * @param id component id
	 * @param referer the class that is refering; is used as the relative
	 * root for gettting the resource
	 * @param file reference as a string
	 */
	public StyleSheetReference(String id, Class referer, String file)
	{
		super(id, referer, file, "href");
	}

	/**
	 * Construct.
	 * @param id component id
	 * @param referer the class that is refering; is used as the relative
	 * root for gettting the resource
	 * @param file reference. The model must provide an instance
	 * 		of {@link String}
	 */
	public StyleSheetReference(String id, Class referer, IModel file)
	{
		super(id, referer, file, "href");
	}

	/**
	 * Construct.
	 * @param id component id
	 * @param resourceReference resource reference
	 */
	public StyleSheetReference(String id, ResourceReference resourceReference)
	{
		super(id, resourceReference, "href");
	}

	/**
	 * Construct.
	 * @param id component id
	 * @param resourceReference resource reference.  The model must provide an instance
	 * 		of {@link ResourceReference}
	 */
	public StyleSheetReference(String id, IModel resourceReference)
	{
		super(id, resourceReference, "href");
	}

	/**
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(ComponentTag tag)
	{
		// Must be attached to a style tag
		checkComponentTag(tag, "link");
		ValueMap attributes = tag.getAttributes();
		attributes.put("rel", "stylesheet");
		attributes.put("type", "text/css");
	}
}
