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
package org.apache.wicket.util.resource.locator.caching;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.resource.UrlResourceStream;

/**
 * A reference which may be used to recreate {@link UrlResourceStream}
 */
class UrlResourceStreamReference extends AbstractResourceStreamReference
{
	private final String url;

	UrlResourceStreamReference(final UrlResourceStream urlResourceStream)
	{
		url = urlResourceStream.getURL().toExternalForm();
		saveResourceStream(urlResourceStream);
	}

	@Override
	public UrlResourceStream getReference()
	{
		try
		{
			UrlResourceStream resourceStream = new UrlResourceStream(new URL(url));
			restoreResourceStream(resourceStream);
			return resourceStream;
		}
		catch (MalformedURLException e)
		{
			// should not ever happen. The cached url is created by previously existing URL
			// instance
			throw new WicketRuntimeException(e);
		}
	}
}