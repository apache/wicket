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
package org.apache.wicket.settings.def;

import org.apache.wicket.markup.MarkupFactory;
import org.apache.wicket.util.lang.Args;

/**
 * Interface for markup related settings.
 * <p>
 * <i>compressWhitespace </i> (defaults to false) - Causes pages to render with redundant whitespace
 * removed. Whitespace stripping is not HTML or JavaScript savvy and can conceivably break pages,
 * but should provide significant performance improvements.
 * <p>
 * <i>stripComments</i> (defaults to false) - Set to true to strip HTML comments during markup
 * loading
 *
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class MarkupSettings
{
	/** Application default for automatically resolving hrefs */
	private boolean automaticLinking = false;

	/** True if multiple tabs/spaces should be compressed to a single space */
	private boolean compressWhitespace = false;

	/** Default markup encoding. If null, the OS default will be used */
	private String defaultMarkupEncoding;

	/** Factory for creating markup parsers */
	private MarkupFactory markupFactory;

	/** if true than throw an exception if the xml declaration is missing from the markup file */
	private boolean throwExceptionOnMissingXmlDeclaration = false;

	/** Should HTML comments be stripped during rendering? */
	private boolean stripComments = false;

	/**
	 * If true, wicket tags ( <wicket: ..>) and wicket:id attributes we be removed from output
	 */
	private boolean stripWicketTags = false;

	/**
	 * Construct
	 */
	public MarkupSettings()
	{
	}

	/**
	 * If true, automatic link resolution is enabled. Disabled by default.
	 *
	 * @see org.apache.wicket.markup.resolver.AutoLinkResolver
	 * @see org.apache.wicket.markup.parser.filter.WicketLinkTagHandler
	 * @return Returns the automaticLinking.
	 */
	public boolean getAutomaticLinking()
	{
		return automaticLinking;
	}

	/**
	 * @return Returns the compressWhitespace.
	 */
	public boolean getCompressWhitespace()
	{
		return compressWhitespace;
	}

	/**
	 * @since 1.1
	 * @return Returns default encoding of markup files. If null, the operating system provided
	 *         encoding will be used.
	 */
	public String getDefaultMarkupEncoding()
	{
		return defaultMarkupEncoding;
	}

	/**
	 * Get the markup factory
	 *
	 * @return A new instance of MarkupFactory.
	 */
	public MarkupFactory getMarkupFactory()
	{
		if (markupFactory == null)
		{
			markupFactory = new MarkupFactory();
		}
		return markupFactory;
	}

	/**
	 * @return Returns the stripComments.
	 */
	public boolean getStripComments()
	{
		return stripComments;
	}

	/**
	 * Gets whether to remove wicket tags from the output.
	 *
	 * @return whether to remove wicket tags from the output
	 */
	public boolean getStripWicketTags()
	{
		return stripWicketTags;
	}

	/**
	 * @since 1.3
	 * @return if true, an exception is thrown if the markup file does not contain a xml declaration
	 */
	public boolean getThrowExceptionOnMissingXmlDeclaration()
	{
		return throwExceptionOnMissingXmlDeclaration;
	}

	/**
	 * Application default for automatic link resolution.
	 *
	 * @param automaticLinking
	 *            The automaticLinking to set.
	 * @see org.apache.wicket.markup.resolver.AutoLinkResolver
	 * @see org.apache.wicket.markup.parser.filter.WicketLinkTagHandler
	 */
	public void setAutomaticLinking(boolean automaticLinking)
	{
		this.automaticLinking = automaticLinking;
	}

	/**
	 * Turns on whitespace compression. Multiple occurrences of space/tab characters will be
	 * compressed to a single space. Multiple line breaks newline/carriage-return will also be
	 * compressed to a single newline.
	 * <p>
	 * Compression is currently not HTML aware and so it may be possible for whitespace compression
	 * to break pages. For this reason, whitespace compression is off by default and you should test
	 * your application thoroughly after turning whitespace compression on.
	 * <p>
	 * Spaces are removed from markup at markup load time and there should be no effect on page
	 * rendering speed. In fact, your pages should render faster with whitespace compression
	 * enabled.
	 *
	 * @param compressWhitespace
	 *            The compressWhitespace to set.
	 */
	public void setCompressWhitespace(final boolean compressWhitespace)
	{
		this.compressWhitespace = compressWhitespace;
	}

	/**
	 * Set default encoding for markup files. If null, the encoding provided by the operating system
	 * will be used.
	 *
	 * @since 1.1
	 * @param encoding
	 */
	public void setDefaultMarkupEncoding(final String encoding)
	{
		defaultMarkupEncoding = encoding;
	}

	/**
	 * Set a new markup factory
	 *
	 * @param factory
	 */
	public void setMarkupFactory(final MarkupFactory factory)
	{
		Args.notNull(factory, "markup factory");
		markupFactory = factory;
	}

	/**
	 * Enables stripping of markup comments denoted in markup by HTML comment tagging.
	 *
	 * @param stripComments
	 *            True to strip markup comments from rendered pages
	 */
	public void setStripComments(boolean stripComments)
	{
		this.stripComments = stripComments;
	}

	/**
	 * Sets whether to remove wicket tags from the output.
	 *
	 * @param stripWicketTags
	 *            whether to remove wicket tags from the output
	 */
	public void setStripWicketTags(boolean stripWicketTags)
	{
		this.stripWicketTags = stripWicketTags;
	}

	/**
	 * If true, an exception is thrown if the markup file does not contain a xml declaration
	 *
	 * @since 1.3
	 * @param throwException
	 */
	public void setThrowExceptionOnMissingXmlDeclaration(boolean throwException)
	{
		throwExceptionOnMissingXmlDeclaration = throwException;
	}
}
