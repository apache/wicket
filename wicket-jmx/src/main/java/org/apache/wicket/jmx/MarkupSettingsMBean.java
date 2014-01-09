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
package org.apache.wicket.jmx;

/**
 * Markup settings.
 * 
 * @author eelcohillenius
 */
public interface MarkupSettingsMBean
{
	/**
	 * If true, automatic link resolution is enabled. Disabled by default.
	 * 
	 * @see org.apache.wicket.markup.resolver.AutoLinkResolver
	 * @see org.apache.wicket.markup.parser.filter.WicketLinkTagHandler
	 * @return Returns the automaticLinking.
	 */
	boolean getAutomaticLinking();

	/**
	 * @return Returns the compressWhitespace.
	 * @see MarkupSettings#setCompressWhitespace(boolean)
	 */
	boolean getCompressWhitespace();

	/**
	 * @since 1.1
	 * @return Returns default encoding of markup files. If null, the operating system provided
	 *         encoding will be used.
	 */
	String getDefaultMarkupEncoding();

	/**
	 * @return Returns the stripComments.
	 * @see MarkupSettings#setStripComments(boolean)
	 */
	boolean getStripComments();

	/**
	 * Gets whether to remove wicket tags from the output.
	 * 
	 * @return whether to remove wicket tags from the output
	 */
	boolean getStripWicketTags();

	/**
	 * Application default for automatic link resolution. Please
	 * 
	 * @see org.apache.wicket.markup.resolver.AutoLinkResolver and
	 * @see org.apache.wicket.markup.parser.filter.WicketLinkTagHandler for more details.
	 * 
	 * @param automaticLinking
	 *            The automaticLinking to set.
	 */
	void setAutomaticLinking(boolean automaticLinking);

	/**
	 * Turns on whitespace compression. Multiple occurrences of space/tab characters will be
	 * compressed to a single space. Multiple line breaks newline/carriage-return will also be
	 * compressed to a single newline.
	 * <p>
	 * Compression is currently not HTML aware and so it may be possible for whitespace compression
	 * to break pages. For this reason, whitespace compression is off by default and you should test
	 * your application throroughly after turning whitespace compression on.
	 * <p>
	 * Spaces are removed from markup at markup load time and there should be no effect on page
	 * rendering speed. In fact, your pages should render faster with whitespace compression
	 * enabled.
	 * 
	 * @param compressWhitespace
	 *            The compressWhitespace to set.
	 */
	void setCompressWhitespace(final boolean compressWhitespace);

	/**
	 * Set default encoding for markup files. If null, the encoding provided by the operating system
	 * will be used.
	 * 
	 * @param encoding
	 */
	void setDefaultMarkupEncoding(final String encoding);

	/**
	 * Enables stripping of markup comments denoted in markup by HTML comment tagging.
	 * 
	 * @param stripComments
	 *            True to strip markup comments from rendered pages
	 */
	void setStripComments(boolean stripComments);

	/**
	 * Sets whether to remove wicket tags from the output.
	 * 
	 * @param stripWicketTags
	 *            whether to remove wicket tags from the output
	 */
	void setStripWicketTags(boolean stripWicketTags);
}
