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
package org.apache.wicket.ajax.form;

import org.apache.wicket.Response;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.util.string.JavascriptUtils;

/**
 * A behavior that updates the hosting {@link AbstractTextComponent} (typically
 * {@link TextField} or {@link TextArea}) via ajax when an onkeyup javascript
 * event is triggered. 
 * 
 * Opposed to {@link AjaxFormComponentUpdatingBehavior} with onkeyup event 
 * this behavior ignores events which do not really change the content of the 
 * component (like esc, cursor keys, tabs etc.), which reduces the the amount 
 * of extra requests that will be sent to the server. Also, opposed to 
 * {@link AjaxFormComponentUpdatingBehavior} with onchange/onblur event
 * this behavior sends the request immediately after the keypress.
 * 
 * If you need similiar behavior to other (non-text) form components, use 
 * {@link AjaxFormComponentUpdatingBehavior} with onchange event.
 * 
 * @author Janne Hietam&auml;ki (janne)
 * 
 * @since 1.3
 * @see AjaxFormComponentUpdatingBehavior
 */
public abstract class OnChangeAjaxBehavior extends AjaxFormComponentUpdatingBehavior
{
	/**
	 * Construct.
	 */
	public OnChangeAjaxBehavior()
	{
		super("onkeyup");
	}

	/**
	 * 
	 * @see org.apache.wicket.behavior.AbstractAjaxBehavior#onBind()
	 */
	protected void onBind()
	{
		super.onBind();

		if (!(getComponent() instanceof AbstractTextComponent))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName()
					+ " can only be added to an instance of a AbstractTextComponent");
		}
	}

	protected void onComponentRendered()
	{
		Response response = getComponent().getResponse();
		final String id = getComponent().getMarkupId();
		response.write(JavascriptUtils.SCRIPT_OPEN_TAG);
		response.write("new Wicket.ChangeHandler('" + id + "');");
		response.write(JavascriptUtils.SCRIPT_CLOSE_TAG);
	}
}
