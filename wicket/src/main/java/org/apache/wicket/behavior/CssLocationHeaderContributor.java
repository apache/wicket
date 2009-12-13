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
package org.apache.wicket.behavior;

import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * Returns a new instance of a header contributor referencing a CSS file using one of the following
 * schemes:
 * <ul>
 * <li>Starts with http:// or https:// for an external reference.</li>
 * <li>Starts with "/" for an absolute reference that Wicket will not rewrite.</li>
 * <li>Starts with anything else, which Wicket will automatically prepend to make relative to the
 * context root of your web-app.</li>
 * </ul>
 * 
 * @author Juergen Donnerstag
 */
public class CssLocationHeaderContributor extends AbstractHeaderContributor
{
	private static final long serialVersionUID = 1L;

	private final String location;
	private final String media;

	/**
	 * Construct.
	 * 
	 * @param location
	 *            The location of the css file.
	 */
	public CssLocationHeaderContributor(final String location)
	{
		this(location, null);
	}

	/**
	 * Construct.
	 * 
	 * @param location
	 *            The location of the css file.
	 * @param media
	 *            The media type for this CSS ("print", "screen", etc.)
	 */
	public CssLocationHeaderContributor(final String location, final String media)
	{
		this.location = location;
		this.media = media;
	}

	/**
	 * @see org.apache.wicket.behavior.AbstractHeaderContributor#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
	 */
	@Override
	public void renderHead(IHeaderResponse response)
	{
		if (media == null)
		{
			response.renderCSSReference(returnRelativePath(location));
		}
		else
		{
			response.renderCSSReference(returnRelativePath(location), media);
		}
	}
}
