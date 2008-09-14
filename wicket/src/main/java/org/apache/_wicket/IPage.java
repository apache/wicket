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
package org.apache._wicket;

/**
 * Base interface for pages. The purpose of this interface is to make certain parts of Wicket easier
 * to mock and unit test.
 * 
 * @author Matej Knopp
 */
// TODO: Better name would be nice
public interface IPage extends IComponent
{
	/**
	 * @return A stable identifier for this page map entry
	 */
	public int getNumericId();

	/**
	 * @return The current version number of this page. If the page has been changed once, the
	 *         return value will be 1. If the page has not yet been revised, the version returned
	 *         will be 0, indicating that the page is still in its original state.
	 */
	public int getCurrentVersionNumber();

	/**
	 * @return String The PageMap name
	 */
	public String getPageMapName();
	
	/**
	 * Renders the page 
	 */
	public void renderPage();
}
