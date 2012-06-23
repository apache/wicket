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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/** */
public class EnclosurePage_13 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parameters
	 */
	public EnclosurePage_13(final PageParameters parameters)
	{
		add(new Label("version", getApplication().getFrameworkSettings().getVersion()));

		// Here are two components.

		// Both are contained within <wicket:enclosure child="invisible">.
		// Since the "invisible" component is hidden (in this case due to
		// isRenderAllowed() == false), the expecation is
		// that the whole enclosure will be hidden as well. Furthermore this implies
		// that none of the components within the enclosure will be rendered.

		// However this is not the case. Even though the "shouldntrendereither"
		// component is in the same enclosure and therefore should not be rendered,
		// Wicket attempts to render it. This is evidenced by the fact that we've
		// purposely included a Label that will blow up when its model is loaded.

		// When this page is loaded we get the exception:
		// WicketRuntimeException: ... nonexistentprop

		// Wicket 1.4.x does not have this problem.

		add(new SecuredContainer_13("invisible"));
		add(new WebMarkupContainer("shouldntrendereither").add(new Label("label",
			new PropertyModel<String>(this, "nonexistentprop"))));
	}
}
