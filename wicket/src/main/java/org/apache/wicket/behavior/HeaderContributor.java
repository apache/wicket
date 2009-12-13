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

/**
 * A bunch of static helper methods to add CSS and Javascript to the markup headers
 * 
 * @author Eelco Hillenius
 * @author Matej Knopp
 */
public class HeaderContributor
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
	 */
	public static final AbstractHeaderContributor forCss(final Class<?> scope, final String path)
	{
		return new CssHeaderContributor(scope, path);
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
	public static final AbstractHeaderContributor forCss(final Class<?> scope, final String path,
		final String media)
	{
		return new CssHeaderContributor(scope, path, media);
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor that references
	 * a CSS file that lives in a package.
	 * 
	 * @param reference
	 * 
	 * @return the new header contributor instance
	 */
	public static final AbstractHeaderContributor forCss(final ResourceReference reference)
	{
		return new CssReferenceHeaderContributor(reference);
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
	public static final AbstractHeaderContributor forCss(final ResourceReference reference,
		final String media)
	{
		return new CssReferenceHeaderContributor(reference, media);
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
	public static final AbstractHeaderContributor forCss(final String location)
	{
		return new CssLocationHeaderContributor(location);
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
	public static final AbstractHeaderContributor forCss(final String location, final String media)
	{
		return new CssLocationHeaderContributor(location, media);
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
	 */
	public static final AbstractHeaderContributor forJavaScript(final Class<?> scope,
		final String path)
	{
		return new JavascriptHeaderContributor(scope, path);
	}

	/**
	 * Returns a new instance of {@link HeaderContributor} with a header contributor that references
	 * a java script file that lives in a package.
	 * 
	 * @param reference
	 * 
	 * @return the new header contributor instance
	 */
	public static final AbstractHeaderContributor forJavaScript(final ResourceReference reference)
	{
		return new JavascriptReferenceHeaderContributor(reference);
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
	 */
	public static final AbstractHeaderContributor forJavaScript(final String location)
	{
		return new JavascriptLocationHeaderContributor(location);
	}

	/**
	 * No need to instantiate
	 */
	private HeaderContributor()
	{
	}
}