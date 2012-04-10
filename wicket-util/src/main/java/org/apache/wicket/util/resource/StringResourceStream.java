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
package org.apache.wicket.util.resource;


/**
 * A StringResourceStream is an IResource implementation for strings.
 * 
 * @see org.apache.wicket.util.resource.IResourceStream
 * @see org.apache.wicket.util.watch.IModifiable
 * @author Jonathan Locke
 */
public final class StringResourceStream extends AbstractStringResourceStream
{
	private static final long serialVersionUID = 1L;

	/** The string resource */
	private final CharSequence string;

	/**
	 * Construct.
	 * 
	 * @param string
	 *            The resource string
	 */
	public StringResourceStream(final CharSequence string)
	{
		this(string, null);
	}

	/**
	 * Construct.
	 * 
	 * @param string
	 *            The resource string
	 * @param contentType
	 *            The mime type of this resource, such as "image/jpeg" or "text/html"
	 */
	public StringResourceStream(final CharSequence string, final String contentType)
	{
		super(contentType);
		this.string = string;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return super.toString() + ": " + string.toString();
	}

	/**
	 * @see org.apache.wicket.util.resource.AbstractStringResourceStream#getString()
	 */
	@Override
	protected String getString()
	{
		return string.toString();
	}

	@Override
	public String asString()
	{
		return getString();
	}
}
