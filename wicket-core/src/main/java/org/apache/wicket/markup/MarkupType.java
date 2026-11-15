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
package org.apache.wicket.markup;

import java.io.Serializable;

/**
 * 
 * @author Juergen Donnerstag
 */
public class MarkupType implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Mime string for XHTML */
	public static final String XML_MIME = "application/xhtml+xml";

	/** Mime string for HTML */
	public static final String HTML_MIME = "text/html";

	/** A HTML markup type for web pages */
	public final static MarkupType HTML_MARKUP_TYPE = new MarkupType("html", HTML_MIME);

	private final String extension;

	private final String mimeType;

	/**
	 * Construct.
	 * 
	 * @param extension
	 *            associate markup file extension. "html" by default.
	 * @param mimeType
	 */
	public MarkupType(final String extension, final String mimeType)
	{
		this.extension = extension;
		this.mimeType = mimeType;
	}

	/**
	 * Gets extension.
	 * 
	 * @return extension
	 */
	public final String getExtension()
	{
		return extension;
	}

	/**
	 * Gets mimeType.
	 * 
	 * @return mimeType
	 */
	public final String getMimeType()
	{
		return mimeType;
	}

	@Override
	public String toString()
	{
		return "MarkupType [extension=" + extension + ", mimeType=" + mimeType + "]";
	}
}
