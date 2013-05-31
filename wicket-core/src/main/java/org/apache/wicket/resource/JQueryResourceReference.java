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

import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.settings.IJavaScriptLibrarySettings;


/**
 * The resource reference for the jquery javascript library as released with Wicket. To add a JQuery
 * resource reference to a component, do not use this reference, but use
 * {@link IJavaScriptLibrarySettings#getJQueryReference()} to prevent version conflicts.
 * 
 * @author papegaaij
 */
public class JQueryResourceReference extends JavaScriptResourceReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * jQuery ver. 1.x - works on modern browsers and IE 6/7/8
	 */
	public static final String VERSION_1 = "jquery/jquery-1.10.1.js";

	private static final JQueryResourceReference INSTANCE = new JQueryResourceReference();

	/**
	 * Normally you should not use this method, but use
	 * {@link IJavaScriptLibrarySettings#getJQueryReference()} to prevent version conflicts.
	 * 
	 * @return the single instance of the resource reference
	 */
	public static JQueryResourceReference get()
	{
		return INSTANCE;
	}

	protected JQueryResourceReference()
	{
		super(JQueryResourceReference.class, VERSION_1);
	}
}
