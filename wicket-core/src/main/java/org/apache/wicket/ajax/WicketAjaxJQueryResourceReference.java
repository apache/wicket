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

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * @author hoeve
 */
public class WicketAjaxJQueryResourceReference extends JavaScriptResourceReference
{
	private static final long serialVersionUID = -2918665261694523156L;

	private static final WicketAjaxJQueryResourceReference INSTANCE = new WicketAjaxJQueryResourceReference();

	/**
	 * @return the singleton INSTANCE
	 */
	public static WicketAjaxJQueryResourceReference get()
	{
		return INSTANCE;
	}

	private WicketAjaxJQueryResourceReference()
	{
		super(AbstractDefaultAjaxBehavior.class, "res/js/wicket-ajax-jquery.js");
	}

	@Override
	public List<HeaderItem> getDependencies()
	{
		final ResourceReference wicketEventReference;
		if (Application.exists())
		{
			wicketEventReference = Application.get().getJavaScriptLibrarySettings().getWicketEventReference();
		}
		else
		{
			wicketEventReference = WicketEventJQueryResourceReference.get();
		}
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(wicketEventReference));
		return dependencies;
	}
}
