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

import java.util.Arrays;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;

/**
 * @author hoeve
 */
public class WicketEventJQueryResourceReference extends JavaScriptResourceReference
{
	private static final long serialVersionUID = -2918665261694523156L;

	private static final WicketEventJQueryResourceReference INSTANCE = new WicketEventJQueryResourceReference();

	/**
	 * @return the singleton INSTANCE
	 */
	public static WicketEventJQueryResourceReference get()
	{
		return INSTANCE;
	}

	private WicketEventJQueryResourceReference()
	{
		super(AbstractDefaultAjaxBehavior.class, "res/js/wicket-event-jquery.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies()
	{
		final ResourceReference backingLibraryReference;
		if (Application.exists())
		{
			backingLibraryReference = Application.get()
				.getJavaScriptLibrarySettings()
				.getJQueryReference();
		}
		else
		{
			backingLibraryReference = JQueryResourceReference.get();
		}
		return Arrays.asList(JavaScriptHeaderItem.forReference(backingLibraryReference));
	}
}
