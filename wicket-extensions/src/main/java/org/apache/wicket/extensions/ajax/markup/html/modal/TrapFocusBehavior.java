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
package org.apache.wicket.extensions.ajax.markup.html.modal;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.util.string.CssUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Trap focus inside a component's markup.
 * 
 * @author svenmeier
 */
public class TrapFocusBehavior extends Behavior
{

	/**
	 * Resource key for a CSS class to be applied to the current active focus-trap. 
	 */
	public static final String CSS_CURRENT_KEY = CssUtils.key(TrapFocusBehavior.class, "current");

	private static final long serialVersionUID = 1L;
	
	private static final ResourceReference JS = new JavaScriptResourceReference(
		TrapFocusBehavior.class, "trap-focus.js");

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(JS));
		
		String styleClass = component.getString(CSS_CURRENT_KEY, null, "current-focus-trap");
		
		CharSequence script = String.format("Wicket.trapFocus('%s', '%s');", component.getMarkupId(), styleClass);
		
		response.render(new PriorityHeaderItem(OnDomReadyHeaderItem.forScript(script)));
	}
}
