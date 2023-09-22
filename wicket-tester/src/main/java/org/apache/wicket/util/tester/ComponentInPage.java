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
package org.apache.wicket.util.tester;

import org.apache.wicket.Component;
import org.apache.wicket.markup.IMarkupFragment;

/**
 * Holds the component which is passed to {@link BaseWicketTester}'s
 * {@link BaseWicketTester#startComponentInPage()} methods and meta data about it.
 */
class ComponentInPage
{
	/**
	 * The component id that is used when Wicket instantiates the tested/started component.
	 */
	static final String ID = "testObject";

	/**
	 * The component that is being started.
	 */
	// see https://issues.apache.org/jira/browse/WICKET-1214
	Component component;

	/**
	 * A flag indicating whether the {@link #component} has been instantiated by Wicket.
	 * 
	 * @see {@link BaseWicketTester#startComponentInPage(Class, IMarkupFragment)}.
	 */
	boolean isInstantiated = false;
}
