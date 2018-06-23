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
package org.apache.wicket.protocol.http.request;

import java.util.Arrays;
import java.util.List;

/**
 * UserAgent
 */
enum UserAgent {

	MOZILLA("Mozilla", "Gecko"),

	FIREFOX("Firefox", "Firefox for iOS"),

	INTERNET_EXPLORER("Internet Explorer"),

	OPERA("Opera", "Opera Tablet"),

	CHROME("Chrome", "Chromium", "CriOS"),

	SAFARI("Safari"),

	KONQUEROR("Konqueror"),

	EDGE("Edge");

	/**
	 * A list with strings which has to be in the user agent string.
	 */
	private List<String> uaStrings;

	/**
	 * Construct.
	 * 
	 * @param uaStrings
	 *            a list of user agents
	 */
	UserAgent(String... uaStrings)
	{
		this.uaStrings = Arrays.asList(uaStrings);
	}

	public List<String> getUaStrings()
	{
		return uaStrings;
	}

}
