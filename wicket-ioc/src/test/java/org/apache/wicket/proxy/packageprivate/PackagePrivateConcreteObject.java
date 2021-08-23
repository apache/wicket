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
package org.apache.wicket.proxy.packageprivate;

/**
 * Mock dependency that does not implement an interface.
 * Its visibility is package private (to {@link PackagePrivateTest}) to test a bug
 * described at https://issues.apache.org/jira/browse/WICKET-4324
 */
class PackagePrivateConcreteObject
{
	private String message;

	/**
	 * Empty default constructor. It is required by byte-buddy to create a proxy.
	 */
	public PackagePrivateConcreteObject()
	{

	}

	/**
	 * Constructor
	 *
	 * @param message
	 */
	public PackagePrivateConcreteObject(final String message)
	{
		this.message = message;
	}

	/**
	 * @return message
	 */
	public String getMessage()
	{
		return message;
	}

}
