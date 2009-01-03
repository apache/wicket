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

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.JavascriptPackageResource;

/**
 * A {@link org.apache.wicket.behavior.AbstractHeaderContributor} behavior that is specialized on
 * package resources. If you use this class, you have to pre-register the resources you want to
 * contribute. A shortcut for common cases is to call {@link #forCss(Class, String)} to contribute a
 * package css file or {@link #forJavaScript(Class, String)} to contribute a packaged javascript
 * file. For instance:
 * 
 * <pre>
 * add(HeaderContributor.forCss(MyPanel.class, &quot;mystyle.css&quot;));
 * </pre>
 * 
 * @author Eelco Hillenius
 * @author Matej Knopp
 */
public class HeaderContributor extends AbstractHeaderContributor
{
	private static final long serialVersionUID = 1L;

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
	 * @deprecated please use CSSPackageResource.getHeaderContribution() instead
	 */
	@Deprecated
	public static final HeaderContributor forCss(final Class<?> scope, final String path)
	{
		return CSSPackageResource.getHeaderContribution(scope, path);
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
	 * @deprecated please use CSSPackageResource.getHeaderContribution() instead
	 */
	@Deprecated
	public static final HeaderContributor forCss(final Class<?> scope, final String path,
		final String media)
	{
		return CSSPackageResource.getHeaderContribution(scope, path, media);
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor that references
	 * a CSS file that lives in a package.
	 * 
	 * @param reference
	 * 
	 * @return the new header contributor instance
	 * @deprecated please use CSSPackageResource.getHeaderContribution() instead
	 */
	@Deprecated
	public static final HeaderContributor forCss(final ResourceReference reference)
	{
		return CSSPackageResource.getHeaderContribution(reference);
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor that references
	 * a CSS file that lives in a package.
	 * 
	 * @param reference
	 * @param media
	 *            The media type for this CSS ("print", "screen", etc.)
	 * @return the new header contributor instance
	 * @deprecated please use CSSPackageResource.getHeaderContribution() instead
	 */
	@Deprecated
	public static final HeaderContributor forCss(final ResourceReference reference,
		final String media)
	{
		return CSSPackageResource.getHeaderContribution(reference, media);
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
	 * @deprecated please use CSSPackageResource.getHeaderContribution() instead
	 */
	@Deprecated
	public static final HeaderContributor forCss(final String location)
	{
		return CSSPackageResource.getHeaderContribution(location);
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
	 * @deprecated please use CSSPackageResource.getHeaderContribution() instead
	 */
	@Deprecated
	public static final HeaderContributor forCss(final String location, final String media)
	{
		return CSSPackageResource.getHeaderContribution(location, media);
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor that references
	 * a java script file that lives in a package.
	 * 
	 * @param scope
	 *            The scope of the package resource (typically the class of the caller, or a class
	 *            that lives in the package where the resource lives).
	 * @param path
	 *            The path
	 * @return the new header contributor instance
	 * @deprecated please use JavascriptPackageResource.getHeaderContribution() instead
	 */
	@Deprecated
	public static final HeaderContributor forJavaScript(final Class<?> scope, final String path)
	{
		return JavascriptPackageResource.getHeaderContribution(scope, path);
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor that references
	 * a java script file that lives in a package.
	 * 
	 * @param reference
	 * 
	 * @return the new header contributor instance
	 * @deprecated please use JavascriptPackageResource.getHeaderContribution() instead
	 */
	@Deprecated
	public static final HeaderContributor forJavaScript(final ResourceReference reference)
	{
		return JavascriptPackageResource.getHeaderContribution(reference);
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor referencing a
	 * java script file using one of the following schemes:
	 * <ul>
	 * <li>Starts with http:// or https:// for an external reference.</li>
	 * <li>Starts with "/" for an absolute reference that Wicket will not rewrite.</li>
	 * <li>Starts with anything else, which Wicket will automatically prepend to make relative to
	 * the context root of your web-app.</li>
	 * </ul>
	 * 
	 * @param location
	 *            The location of the java script file.
	 * @return the new header contributor instance
	 * @deprecated please use JavascriptPackageResource.getHeaderContribution() instead
	 */
	@Deprecated
	public static final HeaderContributor forJavaScript(final String location)
	{
		return JavascriptPackageResource.getHeaderContribution(location);
	}

	/**
	 * Resource reference to contribute.
	 */
	private IHeaderContributor headerContributor = null;

	/**
	 * Construct.
	 * 
	 * @param headerContributor
	 *            the header contributor
	 */
	public HeaderContributor(IHeaderContributor headerContributor)
	{
		if (headerContributor == null)
		{
			throw new IllegalArgumentException("header contributor may not be null");
		}
		this.headerContributor = headerContributor;
	}

	/**
	 * @see org.apache.wicket.behavior.AbstractHeaderContributor#getHeaderContributors()
	 */
	@Override
	public final IHeaderContributor[] getHeaderContributors()
	{
		return new IHeaderContributor[] { headerContributor };
	}
}