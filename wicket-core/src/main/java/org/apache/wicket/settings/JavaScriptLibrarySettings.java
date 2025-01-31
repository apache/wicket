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
package org.apache.wicket.settings;

import jakarta.annotation.Nonnull;

import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * Class for settings related to the JavaScript libraries that come with and are used by Wicket.
 * <p>
 * With these settings the user application can replace the JavaScript libraries used for Wicket's
 * event and Ajax functionality. By default Wicket uses {@linkplain JQueryResourceReference JQuery}
 * as a backing library but via this interface the application can replace the implementations of
 * wicket-event.js, wicket-ajax.js and wicket-ajax-debug.js to use implementations on other
 * libraries, such as YUI or DOJO. The resource reference implementations need to specify the
 * {@linkplain ResourceReference#getDependencies() dependency} on the backing library, if needed.
 *
 * @since 6.0
 */
public class JavaScriptLibrarySettings
{
	private ResourceReference jQueryReference = JQueryResourceReference.getV3();

	private ResourceReference wicketAjaxReference = WicketAjaxJQueryResourceReference.get();

	/**
	 * @return the reference to the JQuery JavaScript library used as backing library for
	 *         wicket-event and wicket-ajax
	 */
	public ResourceReference getJQueryReference()
	{
		return jQueryReference;
	}

	/**
	 * @param jQueryReference
	 *            a reference to the JQuery JavaScript library used as backing library for
	 *            wicket-event and wicket-ajax
	 * @return {@code this} object for chaining
	 */
	public JavaScriptLibrarySettings setJQueryReference(@Nonnull ResourceReference jQueryReference)
	{
		this.jQueryReference = Args.notNull(jQueryReference, "jQueryReference");
		return this;
	}

	/**
	 * @return the reference to the implementation of wicket-ajax.js
	 */
	public ResourceReference getWicketAjaxReference()
	{
		return wicketAjaxReference;
	}

	/**
	 * @param wicketAjaxReference
	 *            a reference to the implementation of wicket-ajax.js
	 * @return {@code this} object for chaining
	 */
	public JavaScriptLibrarySettings setWicketAjaxReference(@Nonnull ResourceReference wicketAjaxReference)
	{
		this.wicketAjaxReference = Args.notNull(wicketAjaxReference, "wicketAjaxReference");
		return this;
	}
}
