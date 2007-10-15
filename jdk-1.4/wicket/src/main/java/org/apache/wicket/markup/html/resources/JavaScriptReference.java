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
package org.apache.wicket.markup.html.resources;

import org.apache.wicket.Application;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.value.IValueMap;

/**
 * Link to a packaged JavaScript file.
 *
 * @author Eelco Hillenius
 */
public class JavaScriptReference extends PackagedResourceReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 *
	 * @param id
	 *            component id
	 * @param referer
	 *            the class that is referring; is used as the relative root for
	 *            getting the resource
	 * @param file
	 *            reference as a string
	 */
	public JavaScriptReference(String id, Class referer, String file)
	{
		super(id, referer, file, "src");
	}

	/**
	 * Construct.
	 *
	 * @param id
	 *            component id
	 * @param referer
	 *            the class that is referring; is used as the relative root for
	 *            getting the resource
	 * @param file
	 *            reference as a string. The model must provide an instance of
	 *            {@link String}
	 */
	public JavaScriptReference(String id, Class referer, IModel file)
	{
		super(id, referer, file, "src");
	}

	/**
	 * Construct.
	 *
	 * @param id
	 *            component id
	 * @param resourceReference
	 *            resource reference
	 */
	public JavaScriptReference(String id, ResourceReference resourceReference)
	{
		super(id, resourceReference, "src");
	}

	/**
	 * Construct.
	 *
	 * @param id
	 *            component id
	 * @param resourceReference
	 *            resource reference. The model must provide an instance of
	 *            {@link ResourceReference}
	 */
	public JavaScriptReference(String id, IModel resourceReference)
	{
		super(id, resourceReference, "src");
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(ComponentTag tag)
	{
		// Must be attached to a script tag
		checkComponentTag(tag, "script");
		IValueMap attributes = tag.getAttributes();
		attributes.put("type", "text/javascript");
	}

	/**
	 * @see org.apache.wicket.markup.html.resources.PackagedResourceReference#createPackageResourceReference(org.apache.wicket.Application,
	 *      java.lang.Class, java.lang.String)
	 */
	protected ResourceReference createPackageResourceReference(Application app, Class scope, String name)
	{
		CompressedResourceReference compressedResourceReference = new CompressedResourceReference(scope, name);
		compressedResourceReference.bind(app);
		return compressedResourceReference;
	}
}