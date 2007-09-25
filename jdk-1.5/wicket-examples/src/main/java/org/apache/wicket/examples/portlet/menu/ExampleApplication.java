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
package org.apache.wicket.examples.portlet.menu;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Ate Douma
 */
public class ExampleApplication implements Serializable
{
	private final String displayName;
	private final String filterPath;
	private final String filterQuery;
	private final Map initParameters;
	private final String description;
	
	public ExampleApplication(String displayName, String filterPath, String filterQuery, Map initParameters, String description)
	{
		this.displayName = displayName;
		this.filterPath = filterPath;
		this.filterQuery = filterQuery;
		this.initParameters = initParameters;
		this.description = description;
	}

	/**
	 * Gets displayName.
	 * @return displayName
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * Gets description.
	 * @return description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Gets filterPath.
	 * @return filterPath
	 */
	public String getFilterPath()
	{
		return filterPath;
	}

	/**
	 * Gets filterQuery.
	 * @return filterQuery
	 */
	public String getFilterQuery()
	{
		return filterQuery;
	}

	/**
	 * Gets initParameter.
	 * @param name initParameter name
	 * @return initParameter
	 */
	public String getInitParameter(String name)
	{
		return (String)initParameters.get(name);
	}
	
	
}
