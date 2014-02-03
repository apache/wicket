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

import org.apache.wicket.ajax.WicketAjaxDebugJQueryResourceReference;
import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.ajax.WicketEventJQueryResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * Interface for settings related to the JavaScript libraries that come with and are used by Wicket.
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
	private ResourceReference jQueryReference = JQueryResourceReference.get();

	private ResourceReference wicketEventReference = WicketEventJQueryResourceReference.get();

	private ResourceReference wicketAjaxReference = WicketAjaxJQueryResourceReference.get();

	private ResourceReference wicketAjaxDebugReference = WicketAjaxDebugJQueryResourceReference.get();

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
	public JavaScriptLibrarySettings setJQueryReference(ResourceReference jQueryReference)
	{
		this.jQueryReference = Args.notNull(jQueryReference, "jQueryReference");
		return this;
	}

	/**
	 * @return the reference to the implementation of wicket-event.js
	 */
	public ResourceReference getWicketEventReference()
	{
		return wicketEventReference;
	}

	/**
	 * @param wicketEventReference
	 *            a reference to the implementation of wicket-event.js
	 * @return {@code this} object for chaining
	 */
	public JavaScriptLibrarySettings setWicketEventReference(ResourceReference wicketEventReference)
	{
		this.wicketEventReference = Args.notNull(wicketEventReference, "wicketEventReference");
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
	public JavaScriptLibrarySettings setWicketAjaxReference(ResourceReference wicketAjaxReference)
	{
		this.wicketAjaxReference = Args.notNull(wicketAjaxReference, "wicketAjaxReference");
		return this;
	}

	/**
	 * The Wicket Ajax Debug Window.
	 *
	 * @return the reference to the implementation of wicket-ajax-debug.js
	 */
	public ResourceReference getWicketAjaxDebugReference()
	{
		return wicketAjaxDebugReference;
	}

	/**
	 * @param wicketAjaxDebugReference
	 *            a reference to the implementation of wicket-ajax-debug.js
	 * @return {@code this} object for chaining
	 */
	public JavaScriptLibrarySettings setWicketAjaxDebugReference(ResourceReference wicketAjaxDebugReference)
	{
		this.wicketAjaxDebugReference = Args.notNull(wicketAjaxDebugReference,
			"wicketAjaxDebugReference");
		return this;
	}

}
