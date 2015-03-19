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
package org.apache.wicket.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.wicket.util.value.ValueMap;

/**
 * Load properties from properties file via a Reader, which allows to provide the charset and thus
 * the encoding can be different than ISO 8859-1.
 * 
 * @author Juergen Donnerstag
 */
public class UtfPropertiesFilePropertiesLoader implements IPropertiesLoader
{
	private final String fileExtension;

	private final String encoding;

	/**
	 * Construct.
	 * 
	 * @param fileExtension
	 * @param encoding
	 */
	public UtfPropertiesFilePropertiesLoader(final String fileExtension, final String encoding)
	{
		this.fileExtension = fileExtension;
		this.encoding = encoding;
	}

	/**
	 * @see org.apache.wicket.resource.IPropertiesLoader#getFileExtension()
	 */
	@Override
	public final String getFileExtension()
	{
		return fileExtension;
	}

	/**
	 * @see org.apache.wicket.resource.IPropertiesLoader#loadJavaProperties(java.io.InputStream)
	 */
	@Override
	public java.util.Properties loadJavaProperties(final InputStream in) throws IOException
	{
		java.util.Properties properties = new java.util.Properties();
		Reader reader = new InputStreamReader(in, encoding);

		properties.load(reader);

		return properties;
	}

	/**
	 * @see org.apache.wicket.resource.IPropertiesLoader#loadWicketProperties(java.io.InputStream)
	 */
	@Override
	public ValueMap loadWicketProperties(InputStream inputStream)
	{
		return null;
	}
}