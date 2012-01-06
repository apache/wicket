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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;

/**
 * Decorator that can be used to cancel the regular action if ajax call was performed. This allows
 * us to, for example, cancel the default anchor behavior (requesting href url) if an ajax call was
 * made in the onclick event handler. Ajax call cannot be performed if javascript has been turned
 * off or no compatible XmlHttpRequest object can be found. This decorator will make javascript
 * return true if the ajax call was made, and false otherwise.
 * 
 * @see AjaxFallbackLink
 * 
 * @since 6.0
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public final class CancelEventIfAjaxListener extends AjaxCallListener
{
	private static final long serialVersionUID = 1L;

	@Override
	public CharSequence getBeforeHandler(Component component)
	{
		return "if (attrs.event) { attrs.event.preventDefault(); }";
	}
}
