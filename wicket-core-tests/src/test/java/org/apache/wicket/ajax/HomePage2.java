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

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Homepage WICKET-2328
 */
public class HomePage2 extends WebPage
{

	private static final long serialVersionUID = 1L;

	private String _message = "clicked";

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public HomePage2(final PageParameters parameters)
	{

		setOutputMarkupId(true);
		add(new Label("msg", new PropertyModel<String>(this, "_message")));
		add(new AjaxLink<Void>("link")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget tgt)
			{
				_message = "ajax link clicked";

				// Adding the entire page to the ajax request target
				// should force the entire page to be re-requested by the
				// browser, thereby reverting the _message value.
				tgt.add(getPage());

				// And it works. However the when testing this behavior
				// with WicketTester, the _message value does not revert.
				// See TestHomePage.java.
			}
		});
	}

	@Override
	protected void onBeforeRender()
	{
		_message = "onBeforeRender called";
		super.onBeforeRender();
	}
}
