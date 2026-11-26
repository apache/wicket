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
package org.apache.wicket.dbsc;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;

/**
 * Starts the binding from a link, the way an application would after sign-in, and offers the other
 * requests the tests send on a bound session.
 */
public class StartPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	static final AtomicInteger AJAX_CLICKS = new AtomicInteger();

	/**
	 * Constructor.
	 */
	public StartPage()
	{
		add(new Link<Void>("start")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				DeviceBoundSessions.get().start();
			}
		});
		add(new Link<Void>("replaceAndStart")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				Session.get().replaceSession();
				DeviceBoundSessions.get().start();
			}
		});
		add(new Link<Void>("replace")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				Session.get().replaceSession();
			}
		});
		add(new Link<Void>("invalidate")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				Session.get().invalidate();
			}
		});
		add(new AjaxLink<Void>("ajax")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				AJAX_CLICKS.incrementAndGet();
			}
		});
	}
}
