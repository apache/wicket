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

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.IAjaxSettings;

/**
 * @since 6.0
 */
public class AjaxSettings implements IAjaxSettings
{
	private ResourceReference backingLibraryReference = new JavaScriptResourceReference(
		AbstractDefaultAjaxBehavior.class, "res/js/jquery/jquery.min.js");

	private ResourceReference wicketEventReference = new JavaScriptResourceReference(
		AbstractDefaultAjaxBehavior.class, "res/js/wicket-event-jquery.js");

	private ResourceReference wicketAjaxReference = new JavaScriptResourceReference(
		AbstractDefaultAjaxBehavior.class, "res/js/wicket-ajax-jquery.js");

	private ResourceReference wicketAjaxDebugReference = new JavaScriptResourceReference(
		AbstractDefaultAjaxBehavior.class, "res/js/wicket-ajax-jquery-debug.js");

	@Override
	public ResourceReference getBackingLibraryReference()
	{
		return backingLibraryReference;
	}

	@Override
	public void setBackingLibraryReference(ResourceReference backingLibraryReference)
	{
		this.backingLibraryReference = backingLibraryReference;
	}

	@Override
	public ResourceReference getWicketEventReference()
	{
		return wicketEventReference;
	}

	@Override
	public void setWicketEventReference(ResourceReference wicketEventReference)
	{
		this.wicketEventReference = wicketEventReference;
	}

	@Override
	public ResourceReference getWicketAjaxReference()
	{
		return wicketAjaxReference;
	}

	@Override
	public void setWicketAjaxReference(ResourceReference wicketAjaxReference)
	{
		this.wicketAjaxReference = wicketAjaxReference;
	}

	@Override
	public ResourceReference getWicketAjaxDebugReference()
	{
		return wicketAjaxDebugReference;
	}

	@Override
	public void setWicketAjaxDebugReference(ResourceReference wicketAjaxDebugReference)
	{
		this.wicketAjaxDebugReference = wicketAjaxDebugReference;
	}

}
