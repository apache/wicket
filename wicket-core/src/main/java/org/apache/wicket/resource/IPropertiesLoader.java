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

import org.apache.wicket.util.value.ValueMap;


/**
 * Property loaders as used by PropertiesFactory to load properties
 * 
 * @author Juergen Donnerstag
 */
public interface IPropertiesLoader
{
	/**
	 * Load the properties into a ValueMap.
	 * 
	 * @param inputStream
	 * @return Properties. Null if not applicable or not found or ...
	 */
	ValueMap loadWicketProperties(final InputStream inputStream);

	/**
	 * Load the properties into a java.util.Properties object
	 * 
	 * @param inputStream
	 * @return Properties. Null if not applicable or not found or ...
	 * @throws IOException
	 */
	java.util.Properties loadJavaProperties(final InputStream inputStream) throws IOException;

	/**
	 * @return The file extension this loader should be applied to
	 */
	String getFileExtension();
}