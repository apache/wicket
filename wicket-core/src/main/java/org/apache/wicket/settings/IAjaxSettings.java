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

import org.apache.wicket.request.resource.ResourceReference;


/**
 * Interface for Ajax related settings.
 * <p>
 * With these settings the user application can replace the JavaScript libraries used for Wicket
 * Ajax functionality. By default Wicket uses JQuery as a backing library but with
 * {@link #setBackingLibraryReference(ResourceReference)} the application can either replace with
 * Dojo, YUI, ... or just use a different version of JQuery. If the backing library is replaced with
 * another one then the user application will need to provide implementation of Wicket JavaScript
 * APIs implemented with the new backing library, i.e. will need to set different resource
 * references for wicket-event.js and wicket-ajax.js
 * 
 * @since 6.0
 */
public interface IAjaxSettings
{
	/**
	 * @return the reference to the used backing library
	 */
	ResourceReference getBackingLibraryReference();

	/**
	 * @param reference
	 *            a reference to the backing library
	 */
	void setBackingLibraryReference(ResourceReference reference);

	/**
	 * @return the reference to the implementation of wicket-event.js
	 */
	ResourceReference getWicketEventReference();

	/**
	 * @param reference
	 *            a reference to the implementation of wicket-event.js
	 */
	void setWicketEventReference(ResourceReference reference);

	/**
	 * @return the reference to the implementation of wicket-ajax.js
	 */
	ResourceReference getWicketAjaxReference();

	/**
	 * @param reference
	 *            a reference to the implementation of wicket-ajax.js
	 */
	void setWicketAjaxReference(ResourceReference reference);

	/**
	 * The Wicket Ajax Debug Window.
	 * 
	 * @return the reference to the implementation of wicket-ajax-debug.js
	 */
	ResourceReference getWicketAjaxDebugReference();

	/**
	 * @param reference
	 *            a reference to the implementation of wicket-ajax-debug.js
	 */
	void setWicketAjaxDebugReference(ResourceReference reference);
}
