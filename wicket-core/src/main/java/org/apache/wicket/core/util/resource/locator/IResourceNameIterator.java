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

import java.util.Iterator;
import java.util.Locale;

/**
 * Contains the logic to locate a resource based on a path, style (see
 * {@link org.apache.wicket.Session}), variation, locale and extension strings.
 * 
 * @author Juergen Donnerstag
 */
public interface IResourceNameIterator extends Iterator<String>
{
	/**
	 * Get the exact Locale which has been used for the latest resource path.
	 * 
	 * @return current Locale
	 */
	public Locale getLocale();

	/**
	 * Get the exact Style which has been used for the latest resource path.
	 * 
	 * @return current Style
	 */
	public String getStyle();

	/**
	 * Get the exact Variation which has been used for the latest resource path.
	 * 
	 * @return current Variation
	 */
	public String getVariation();

	/**
	 * Get the exact filename extension used for the latest resource path.
	 * 
	 * @return current filename extension
	 */
	public String getExtension();
}
