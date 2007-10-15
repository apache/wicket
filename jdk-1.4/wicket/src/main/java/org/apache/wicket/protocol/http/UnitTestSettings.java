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
package org.apache.wicket.protocol.http;

/**
 * Holds settings for rendering responses under unit testing.
 * 
 * @author Jonathan
 */
public class UnitTestSettings
{
	/**
	 * True when unit testing is going on and we need to sort coded parameters so they will string
	 * compare.
	 */
	private static boolean sortUrlParameters = false;

	/**
	 * @return True if URL parameters should be sorted
	 */
	public static boolean getSortUrlParameters()
	{
		return UnitTestSettings.sortUrlParameters;
	}

	/**
	 * @param sortUrlParameters
	 *            True if URL parameters should be sorted
	 */
	public static void setSortUrlParameters(boolean sortUrlParameters)
	{
		UnitTestSettings.sortUrlParameters = sortUrlParameters;
	}
}
