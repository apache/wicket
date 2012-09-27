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
package org.apache.wicket.feedback;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

/**
 */
public class FeedbacksPage extends WebPage
{

	private static final long serialVersionUID = 1L;

	public final StringBuilder onBeforeRenderOrder = new StringBuilder();

	/**
	 */
	public FeedbacksPage()
	{

		Impl impl1 = new FeedbackImpl("id1");
		add(impl1);

		Impl impl2 = new FeedbackImpl("id2");
		impl1.add(impl2);

		Impl impl3 = new FeedbackImpl("id3");
		impl2.add(impl3);

		Impl impl4 = new Impl("id4");
		add(impl4);
	}

	private class Impl extends WebMarkupContainer
	{

		private static final long serialVersionUID = 1L;

		private Impl(String id)
		{
			super(id);
		}

		@Override
		protected void onBeforeRender()
		{
			onBeforeRenderOrder.append("|");
			onBeforeRenderOrder.append(getId());

			super.onBeforeRender();
		}
	}

	private class FeedbackImpl extends Impl implements IFeedback
	{

		private static final long serialVersionUID = 1L;

		private FeedbackImpl(String id)
		{
			super(id);
		}
	}
}
