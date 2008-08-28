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
package org.apache.wicket.ajaxng;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * @author Matej Knopp
 */
public abstract class AjaxEventBehavior extends AjaxBehavior
{
	private static final long serialVersionUID = 1L;

	private final String event;

	/**
	 * Construct.
	 * 
	 * @param event
	 *            Event on which the behavior should be executed. The event needs to be specified
	 *            without the "on" prefix (e.g. <code>click</code>, <code>change</code>)
	 */
	public AjaxEventBehavior(String event)
	{
		this.event = event;
	}

	@Override
	public final void respond(AjaxRequestTarget target)
	{
		onEvent(target);
	}
	
	protected abstract void onEvent(AjaxRequestTarget target);
	
	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		StringBuilder js = new StringBuilder();
		js.append(AjaxBehavior.WICKET_NS + ".e('");
		js.append(event);
		js.append("',");
		js.append(renderAttributes(component));
		
		boolean allowDefault = getAttributes().isAllowDefault(); 
		if (allowDefault)
		{
			js.append("," + allowDefault);
		}
		js.append(")");				

		response.renderOnDomReadyJavascript(decorateScript(js).toString());
	}
	
}
