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
package org.apache.wicket.request.resource;

import org.apache.wicket.request.Url;
import org.apache.wicket.util.lang.Args;

/**
 * A ResourceReference that can be used to render a Url to a resource out of the
 * current application, for example to a resource residing in a CDN (Content Delivering Network).
 *
 * @since 6.0
 */
public class ExternalUrlResourceReference extends ResourceReference
{
	/**
	 * The url to the resource.
	 */
	private final Url externalUrl;

	/**
	 * Constructor.
	 *
	 * @param externalUrl
	 *      the url of the external resource
	 */
	public ExternalUrlResourceReference(final Url externalUrl)
	{
		super(asName(externalUrl));

		if (externalUrl.isAbsolute() == false)
		{
			throw new IllegalArgumentException(ExternalUrlResourceReference.class.getSimpleName() +
					" can be used only with absolute urls.");
		}

		this.externalUrl = externalUrl;
	}

	private static String asName(Url externalUrl)
	{
		Args.notNull(externalUrl, "externalUrl");
		return externalUrl.toString();
	}

	/**
	 * @return the url of the external resource
	 */
	public final Url getUrl()
	{
		return externalUrl;
	}

	/**
	 * @return {@code null} because this ResourceReference wont use an IResource to deliver
	 *  the content of the external resource. The browser will make a direct request to the
	 *  external url.
	 */
	@Override
	public IResource getResource()
	{
		return null;
	}
}
