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
package org.apache.wicket.csp;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;

/**
 * A CSP value that renders an URI relative to the context root of the Wicket application.
 * 
 * @author papegaaij
 */
public class RelativeURICSPValue implements CSPRenderable
{
	private final String relativeUri;

	/**
	 * Creates a new {@code RelativeURICSPValue} for the given relative URI.
	 * 
	 * @param relativeUri
	 *            The part of the URI relative to the context root of the Wicket application.
	 */
	public RelativeURICSPValue(String relativeUri)
	{
		Args.notEmpty(relativeUri, "relativeUri");
		this.relativeUri = relativeUri;
	}

	@Override
	public String render(ContentSecurityPolicySettings listener, RequestCycle cycle,
	                     IRequestHandler currentHandler)
	{
		return cycle.getUrlRenderer().renderContextRelativeUrl(relativeUri);
	}

	@Override
	public void checkValidityForSrc()
	{
		try
		{
			new URI("https://example.com/" + relativeUri);
		}
		catch (URISyntaxException urise)
		{
			throw new IllegalArgumentException("Illegal relative URI", urise);
		}
	}

	@Override
	public String toString()
	{
		return relativeUri;
	}
}
