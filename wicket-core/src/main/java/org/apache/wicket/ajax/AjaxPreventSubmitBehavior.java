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
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.form.TextField;

/**
 * <p>
 *     An Ajax behavior that could be used to prevent the form submit
 *     when the user presses the <em>ENTER</em> key while in an HTML
 *     &lt;input type="text" &gt; field.
 * </p>
 * <p>
 *     The behavior could be applied to a {@link TextField} or to a parent
 *     container that will apply this behavior to all children of type HTMLInputElement.
 * </p>
 * <p>
 *     This Ajax behavior is client-side only, i.e. it never fires an Ajax call to the server.
 * </p>
 */
public class AjaxPreventSubmitBehavior extends AjaxEventBehavior {

	/**
	 * Constructor.
	 */
	public AjaxPreventSubmitBehavior() {
		super("keydown");
	}

	@Override
	protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);

		Component component = getComponent();
		if (component instanceof TextField<?> == false)
		{
			attributes.setChildSelector("input");
		}

		AjaxCallListener listener = new AjaxCallListener();
		listener.onPrecondition("if (Wicket.Event.keyCode(attrs.event) === 13) {attrs.event.preventDefault();} return false;");
		attributes.getAjaxCallListeners().add(listener);
	}

	@Override
	protected final void onEvent(final AjaxRequestTarget target) {
		// never called
	}
}
