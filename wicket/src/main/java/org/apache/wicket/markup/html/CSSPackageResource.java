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
package org.apache.wicket.markup.html;

import java.util.Locale;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;


/**
 * Package resource for CSS files. It is not different than PackageResource except that it provide
 * utility methods to create proper HTML header contributions for CSS files
 * 
 * @author Juergen Donnerstag
 */
public class CSSPackageResource extends PackageResource
{
	private static final long serialVersionUID = 1L;;

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor that references
	 * a CSS file that lives in a package.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the caller, or a class
	 *            that lives in the package where the resource lives).
	 * @param path
	 *            The path
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor getHeaderContribution(final Class<?> scope,
		final String path)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderCSSReference(new CompressedResourceReference(scope, path));
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor that references
	 * a CSS file that lives in a package.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the caller, or a class
	 *            that lives in the package where the resource lives).
	 * @param path
	 *            The path
	 * @param media
	 *            The media type for this CSS ("print", "screen", etc.)
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor getHeaderContribution(final Class<?> scope,
		final String path, final String media)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderCSSReference(new CompressedResourceReference(scope, path), media);
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor that references
	 * a CSS file that lives in a package.
	 * 
	 * @param reference
	 * 
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor getHeaderContribution(final ResourceReference reference)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderCSSReference(reference);
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor that references
	 * a CSS file that lives in a package.
	 * 
	 * @param reference
	 * @param media
	 *            The media type for this CSS ("print", "screen", etc.)
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor getHeaderContribution(final ResourceReference reference,
		final String media)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderCSSReference(reference, media);
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor referencing a
	 * CSS file using one of the following schemes:
	 * <ul>
	 * <li>Starts with http:// or https:// for an external reference.</li>
	 * <li>Starts with "/" for an absolute reference that Wicket will not rewrite.</li>
	 * <li>Starts with anything else, which Wicket will automatically prepend to make relative to
	 * the context root of your web-app.</li>
	 * </ul>
	 * 
	 * @param location
	 *            The location of the css file.
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor getHeaderContribution(final String location)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderCSSReference(returnRelativePath(location));
			}
		});
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor referencing a
	 * CSS file using one of the following schemes:
	 * <ul>
	 * <li>Starts with http:// or https:// for an external reference.</li>
	 * <li>Starts with "/" for an absolute reference that Wicket will not rewrite.</li>
	 * <li>Starts with anything else, which Wicket will automatically prepend to make relative to
	 * the context root of your web-app.</li>
	 * </ul>
	 * 
	 * @param location
	 *            The location of the css.
	 * @param media
	 *            The media type for this CSS ("print", "screen", etc.)
	 * @return the new header contributor instance
	 */
	public static final HeaderContributor getHeaderContribution(final String location,
		final String media)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
				response.renderCSSReference(returnRelativePath(location), media);
			}
		});
	}

	/**
	 * 
	 * @param location
	 * @return relative path
	 */
	private static final String returnRelativePath(String location)
	{
		// WICKET-59 allow external URLs, WICKET-612 allow absolute URLs.
		if (location.startsWith("http://") || location.startsWith("https://") ||
			location.startsWith("/"))
		{
			return location;
		}
		else
		{
			return RequestCycle.get()
				.getProcessor()
				.getRequestCodingStrategy()
				.rewriteStaticRelativeUrl(location);
		}
	}

	/**
	 * Creates a new javascript package resource.
	 * 
	 * @param scope
	 * @param path
	 * @param locale
	 * @param style
	 */
	protected CSSPackageResource(Class<?> scope, String path, Locale locale, String style)
	{
		super(scope, path, locale, style);
	}
}
