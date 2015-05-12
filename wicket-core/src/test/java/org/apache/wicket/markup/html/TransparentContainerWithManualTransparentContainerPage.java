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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;

/**
 * A test page for triggering a StackOverflowError when updating a component inside a
 * {@link TransparentWebMarkupContainer}, with manually added TWMCs to ensure we don't only check
 * for auto-added components, but manually added TWMCs as well while fixing the StackOverflowError
 * bug.
 */
public class TransparentContainerWithManualTransparentContainerPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public TransparentContainerWithManualTransparentContainerPage()
	{
		final Label label = new Label("label", "Label");
		label.setOutputMarkupId(true);
		add(label);

		// adding a TWMC to this link in itself is harmless, but when you have two of these
		// constructions on your page, it causes a StackOverflowError.
		AjaxLink<Void> group1 = new AjaxLink<Void>("group1")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget t)
			{
			}
		};
		add(group1);
		group1.add(new TransparentWebMarkupContainer("twmc_group_1"));

		// This is the second TWMC inside a container construct that triggers the
		// StackOverflowError.
		AjaxLink<Void> group2 = new AjaxLink<Void>("group2")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget t)
			{
			}
		};
		add(group2);
		group2.add(new TransparentWebMarkupContainer("twmc_group_2"));

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

		// if you add this TransparentWebMarkupContainer as first component in
		// the page, the test passes
		add(new TransparentWebMarkupContainer("twmc"));
	}
}
