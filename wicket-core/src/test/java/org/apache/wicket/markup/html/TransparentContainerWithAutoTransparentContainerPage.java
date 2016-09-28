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
package org.apache.wicket.markup.html;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;

/**
 * A test page for triggering a StackOverflowError when updating a component inside a
 * {@link TransparentWebMarkupContainer}, with Wicket auto-adding TWMC's. 
 */
public class TransparentContainerWithAutoTransparentContainerPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public TransparentContainerWithAutoTransparentContainerPage()
	{
		final Label label = new Label("label", "Label");
		label.setOutputMarkupId(true);
		add(label);

		// Letting Wicket auto-add a TransparentWebMarkupContainer to the AJAX link (due to relative
		// URL rewriting) causes a StackOverflowException
		add(new AjaxLink<Void>("group1")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget t)
			{
			}
		});

		// But you need two auto-added TWMCs to trigger the StackOverflowError
		add(new AjaxLink<Void>("group2")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget t)
			{
			}
		});

		// a non-AJAX click on this link passes the test case, an AJAX request
		// fails with a StackOverflowError
		add(new AjaxFallbackLink<Void>("link")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(Optional<AjaxRequestTarget> targetOptional)
			{
				targetOptional.ifPresent(target -> target.add(label));
			}
		});

		// if you add this TransparentWebMarkupContainer as first component in
		// the page, the test passes
		add(new TransparentWebMarkupContainer("twmc"));
	}
}
