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
package org.apache.wicket.core.util.resource.locator;

import java.util.Locale;

import org.apache.wicket.util.resource.IResourceStream;


/**
 * Interface for code that locates resources.
 * 
 * @author Jonathan Locke
 */
public interface IResourceStreamLocator
{
	/**
	 * Locate a resource, given a path and class. Typically this method is either called by external
	 * clients if they are not interested in a lookup that takes the style and locale into account,
	 * or it is called by the implementation of
	 * {@link #locate(Class, String, String, String, java.util.Locale, String, boolean)} where the
	 * latter just takes care of trying out the different combinations for the provided style and
	 * locale and uses this method to actually load the resource stream.
	 * 
	 * @param clazz
	 *            The class loader for delegating the loading of the resource
	 * @param path
	 *            The path of the resource
	 * 
	 * @return The resource or null
	 */
	IResourceStream locate(Class<?> clazz, String path);

	/**
	 * Locate a resource by combining the given path, style, variation, locale and extension
	 * parameters. The exact search order depends on the implementation.
	 * 
	 * @param clazz
	 *            The class loader for delegating the loading of the resource
	 * @param path
	 *            The path of the resource
	 * @param style
	 *            Any resource style, such as a skin style (see {@link org.apache.wicket.Session})
	 * @param variation
	 *            The component's variation (of the style)
	 * @param locale
	 *            The locale of the resource to load
	 * @param extension
	 *            A comma separate list of extensions
	 * @param strict
	 *            whether the specified attributes must match exactly
	 * @return The resource or null
	 */
	IResourceStream locate(Class<?> clazz, String path, String style, String variation,
		Locale locale, String extension, boolean strict);

	/**
	 * Markup resources and Properties files both need to iterate over different combinations of
	 * locale, style, etc.. And though no single locate(..) method exists which is used by both,
	 * they both use ResourceNameIterators.
	 * 
	 * @param path
	 *            The path of the resource
	 * @param style
	 *            Any resource style, such as a skin style (see {@link org.apache.wicket.Session})
	 * @param variation
	 *            The component's variation (of the style)
	 * @param locale
	 *            The locale of the resource to load
	 * @param extension
	 *            A comma separate list of extensions
	 * @param strict
	 *            whether the specified attributes must match exactly
	 * @return resource name iterator
	 */
	IResourceNameIterator newResourceNameIterator(String path, Locale locale, String style,
		String variation, String extension, boolean strict);
}
