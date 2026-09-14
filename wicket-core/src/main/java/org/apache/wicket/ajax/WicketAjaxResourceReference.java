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
package org.apache.wicket.ajax;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

/**
 * A {@link JavaScriptResourceReference} for the plain JavaScript (no jQuery) implementation of
 * Wicket's Ajax and event support. It exposes the exact same {@code Wicket.*} JavaScript API as
 * {@link WicketAjaxJQueryResourceReference}, so it can be used as a drop-in replacement without
 * requiring jQuery to be loaded at all.
 * <p>
 * To switch the whole application to it:
 *
 * <pre>
 * getJavaScriptLibrarySettings().setWicketAjaxReference(WicketAjaxResourceReference.get());
 * </pre>
 *
 * @see org.apache.wicket.settings.JavaScriptLibrarySettings
 * @since 10.10.0
 */
public class WicketAjaxResourceReference extends JavaScriptResourceReference
{
	private static final long serialVersionUID = 1L;

	private static final WicketAjaxResourceReference INSTANCE = new WicketAjaxResourceReference();

	/**
	 * @return the singleton INSTANCE
	 */
	public static WicketAjaxResourceReference get()
	{
		return INSTANCE;
	}

	private WicketAjaxResourceReference()
	{
		super(AbstractDefaultAjaxBehavior.class, "res/js/wicket-ajax.js");
	}
}
