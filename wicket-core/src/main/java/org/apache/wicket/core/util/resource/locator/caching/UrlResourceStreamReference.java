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
package org.apache.wicket.core.util.resource.locator.caching;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.core.util.resource.UrlResourceStream;

/**
 * A reference which may be used to recreate {@link UrlResourceStream}.
 * <p>
 * If the UrlResourceStream deals with URLs with custom {@link java.net.URLStreamHandler}
 * then you will need to export <em>java.protocol.handler.pkgs</em> system property
 * so that the JVM can automatically use them to re-create the URL from its cached
 * external form.
 * </p>
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
			// It can happen when the previously existing URL had a non-standard protocol, and
			// had a custom URLStreamHandler associated with it, which knew how to deal with
			// said protocol. When the URL is recreated from the external form, the
			// URLStreamHandler is no longer associated, and, if the URL has a non-standard
			// protocol for which Java has no handler, a MalformedURLException will be thrown.
			throw new WicketRuntimeException(e);
		}
	}
}