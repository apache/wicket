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

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.IAjaxSettings;
import org.apache.wicket.settings.IDebugSettings;

/**
 * A helper class that contributes all required JavaScript resources needed for Wicket Ajax
 * functionality
 * 
 * @since 6.0
 */
public class CoreLibrariesContributor
{

	/**
	 * Contributes the backing library plus the implementation of Wicket.Event.
	 * 
	 * @param application
	 *            the application instance
	 * @param response
	 *            the current header response
	 */
	public static void contribute(final Application application, final IHeaderResponse response)
	{
		IAjaxSettings ajaxSettings = application.getAjaxSettings();
		ResourceReference backingLibraryReference = ajaxSettings.getBackingLibraryReference();
		ResourceReference wicketEventReference = ajaxSettings.getWicketEventReference();

		response.renderJavaScriptReference(backingLibraryReference);
		response.renderJavaScriptReference(wicketEventReference);

	}

	/**
	 * Contributes the Ajax backing library plus wicket-event.js and wicket-ajax.js implementations.
	 * Additionally if Ajax debug is enabled then wicket-ajax-debug.js implementation is also added.
	 * 
	 * @param application
	 *            the application instance
	 * @param response
	 *            the current header response
	 */
	public static void contributeAjax(final Application application, final IHeaderResponse response)
	{
		CoreLibrariesContributor.contribute(application, response);

		IAjaxSettings ajaxSettings = application.getAjaxSettings();
		ResourceReference wicketAjaxReference = ajaxSettings.getWicketAjaxReference();

		response.renderJavaScriptReference(wicketAjaxReference);

		final IDebugSettings debugSettings = application.getDebugSettings();
		if (debugSettings.isAjaxDebugModeEnabled())
		{
			response.renderJavaScriptReference(ajaxSettings.getWicketAjaxDebugReference());
			response.renderJavaScript("Wicket.Ajax.DebugWindow.enabled=true;",
				"wicket-ajax-debug-enable");
		}
	}
}
