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
package org.apache.wicket.request.resource.caching;

import org.apache.wicket.request.mapper.parameter.INamedParameters;

/**
 * Url view given to the {@link IResourceCachingStrategy} to manipulate
 * 
 * @author igor
 */
public class ResourceUrl
{
	private String fileName;
	private INamedParameters parameters;

	/**
	 * Constructor
	 * 
	 * @param fileName
	 *            file name of the resource
	 * @param urlParameters
	 *            query string parameters
	 */
	public ResourceUrl(String fileName, INamedParameters urlParameters)
	{
		this.fileName = fileName;
		parameters = urlParameters;
	}

	/**
	 * @return file name of the resource
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * @param fileName
	 *            file name of the resource
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	/**
	 * @return query string parameters
	 */
	public INamedParameters getParameters()
	{
		return parameters;
	}

	@Override
	public String toString()
	{
		return "Name: " + fileName + "\n\tParameters: " + parameters;
	}
}
