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
package org.apache.wicket.devutils;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;

/**
 * Just an exception that can be thrown if a development utility is invoked, constructed, etc, and
 * the setting in the application is not enabled.
 * 
 * Includes a static method that also does this check so that it is not carbon copied throughout the
 * code.
 * 
 * If you see this error in your application, you need to call:
 * <tt>Application.get().getDebugSettings().setDevelopmentUtilitiesEnabled(true)</tt>
 * 
 * @author Jeremy Thomerson
 */
public class DevelopmentUtilitiesNotEnabledException extends WicketRuntimeException
{

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public DevelopmentUtilitiesNotEnabledException()
	{
		super("IDebugSettings.developmentUtilitiesEnabled must be enabled to use this feature");
	}

	/**
	 * Verifies that development utilities are enabled.
	 */
	public static void check()
	{
		if (Application.get().getDebugSettings().isDevelopmentUtilitiesEnabled() == false)
		{
			throw new DevelopmentUtilitiesNotEnabledException();
		}
	}
}
