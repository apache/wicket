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
import org.apache.wicket.settings.IMarkupSettings;
import org.apache.wicket.util.lang.Args;

/**
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class MarkupSettings implements IMarkupSettings
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
	 * @see org.apache.wicket.settings.IMarkupSettings#getAutomaticLinking()
	 */
	@Override
	public boolean getAutomaticLinking()
	{
		return automaticLinking;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getCompressWhitespace()
	 */
	@Override
	public boolean getCompressWhitespace()
	{
		return compressWhitespace;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getDefaultMarkupEncoding()
	 */
	@Override
	public String getDefaultMarkupEncoding()
	{
		return defaultMarkupEncoding;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getMarkupFactory()
	 */
	@Override
	public MarkupFactory getMarkupFactory()
	{
		if (markupFactory == null)
		{
			markupFactory = new MarkupFactory();
		}
		return markupFactory;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getStripComments()
	 */
	@Override
	public boolean getStripComments()
	{
		return stripComments;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getStripWicketTags()
	 */
	@Override
	public boolean getStripWicketTags()
	{
		return stripWicketTags;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#getThrowExceptionOnMissingXmlDeclaration()
	 */
	@Override
	public boolean getThrowExceptionOnMissingXmlDeclaration()
	{
		return throwExceptionOnMissingXmlDeclaration;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setAutomaticLinking(boolean)
	 */
	@Override
	public void setAutomaticLinking(boolean automaticLinking)
	{
		this.automaticLinking = automaticLinking;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setCompressWhitespace(boolean)
	 */
	@Override
	public void setCompressWhitespace(final boolean compressWhitespace)
	{
		this.compressWhitespace = compressWhitespace;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setDefaultMarkupEncoding(java.lang.String)
	 */
	@Override
	public void setDefaultMarkupEncoding(final String encoding)
	{
		defaultMarkupEncoding = encoding;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setMarkupFactory(org.apache.wicket.markup.MarkupFactory)
	 */
	@Override
	public void setMarkupFactory(final MarkupFactory factory)
	{
		Args.notNull(factory, "markup factory");
		markupFactory = factory;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setStripComments(boolean)
	 */
	@Override
	public void setStripComments(boolean stripComments)
	{
		this.stripComments = stripComments;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setStripWicketTags(boolean)
	 */
	@Override
	public void setStripWicketTags(boolean stripWicketTags)
	{
		this.stripWicketTags = stripWicketTags;
	}

	/**
	 * @see org.apache.wicket.settings.IMarkupSettings#setThrowExceptionOnMissingXmlDeclaration(boolean)
	 */
	@Override
	public void setThrowExceptionOnMissingXmlDeclaration(boolean throwException)
	{
		throwExceptionOnMissingXmlDeclaration = throwException;
	}
}
