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
package org.apache.wicket.settings.def;

import org.apache.wicket.ajax.WicketAjaxDebugJQueryResourceReference;
import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.ajax.WicketEventJQueryResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;
import org.apache.wicket.settings.IJavaScriptLibrarySettings;
import org.apache.wicket.util.lang.Args;

/**
 * @since 6.0
 */
public class JavaScriptLibrarySettings implements IJavaScriptLibrarySettings
{
	private ResourceReference jQueryReference = JQueryResourceReference.get();

	private ResourceReference wicketEventReference = WicketEventJQueryResourceReference.get();

	private ResourceReference wicketAjaxReference = WicketAjaxJQueryResourceReference.get();

	private ResourceReference wicketAjaxDebugReference = WicketAjaxDebugJQueryResourceReference.get();

	@Override
	public ResourceReference getJQueryReference()
	{
		return jQueryReference;
	}

	@Override
	public void setJQueryReference(ResourceReference jQueryReference)
	{
		this.jQueryReference = Args.notNull(jQueryReference, "jQueryReference");
	}

	@Override
	public ResourceReference getWicketEventReference()
	{
		return wicketEventReference;
	}

	@Override
	public void setWicketEventReference(ResourceReference wicketEventReference)
	{
		this.wicketEventReference = Args.notNull(wicketEventReference, "wicketEventReference");
	}

	@Override
	public ResourceReference getWicketAjaxReference()
	{
		return wicketAjaxReference;
	}

	@Override
	public void setWicketAjaxReference(ResourceReference wicketAjaxReference)
	{
		this.wicketAjaxReference = Args.notNull(wicketAjaxReference, "wicketAjaxReference");
	}

	@Override
	public ResourceReference getWicketAjaxDebugReference()
	{
		return wicketAjaxDebugReference;
	}

	@Override
	public void setWicketAjaxDebugReference(ResourceReference wicketAjaxDebugReference)
	{
		this.wicketAjaxDebugReference = Args.notNull(wicketAjaxDebugReference,
			"wicketAjaxDebugReference");
	}

}
