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
package org.apache.wicket.queueing.transparentresolvers;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * A test page for triggering a StackOverflowError when updating a component inside a
 * {@link TransparentWebMarkupContainer} using AJAX.
 */
public class Wicket5898Page2 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public Wicket5898Page2()
	{
		final Label label = new Label("label", "Label");
		label.setOutputMarkupId(true);
		add(label);

		// Adding a TransparentWebMarkupContainer to the outer TransparentWebMarkupContainer causes
		// a StackOverflowException
		add(new TransparentWebMarkupContainer("group"));

		// if you add this TransparentWebMarkupContainer as first component in
		// the page (i.e. move line 48 to line 38), the test passes
		add(new TransparentWebMarkupContainer("twmc"));

		// a non-AJAX click on this link passes the test case, an AJAX request
		// fails with a StackOverflowError
		add(new AjaxFallbackLink<Void>("link")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				if (target != null)
					target.add(label);
			}
		});
	}
}
