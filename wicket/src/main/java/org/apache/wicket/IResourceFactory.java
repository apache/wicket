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
package org.apache.wicket;

import java.util.Locale;

import org.apache.wicket.request.resource.IResource;

/**
 * A factory which creates resources for a given specification string. The format of the
 * specification string is determined by the resource factory subclass. If the resource factory
 * produced button images, the specification might include settings relevant to buttons such as the
 * width, height and color of the button, as well as the text to draw on the button.
 * 
 * @see org.apache.wicket.markup.html.image.resource.LocalizedImageResource
 * @author Jonathan Locke
 */
public interface IResourceFactory
{
	/**
	 * @param specification
	 *            The resource specification string
	 * @param locale
	 *            The locale for the resource
	 * @param style
	 *            The style of the resource (see {@link org.apache.wicket.Session})
	 * @param variation
	 *            The component's variation
	 * @return The resource
	 */
	IResource newResource(final String specification, final Locale locale, final String style,
		final String variation);
}
