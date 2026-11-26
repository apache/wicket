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
package org.apache.wicket.examples.devicebound;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.dbsc.DeviceBoundSessions;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.examples.homepage.AuthenticationIndex;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

/**
 * Shows whether the session is bound and keeps making Ajax requests on it.
 */
@AuthorizeInstantiation(Roles.USER)
public class DeviceBoundPage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	private int requests;

	/**
	 * Constructor.
	 */
	public DeviceBoundPage()
	{
		WebMarkupContainer status = new WebMarkupContainer("status");
		status.setOutputMarkupId(true);
		add(status);
		status.add(new Label("bound",
			() -> DeviceBoundSessions.get().isBound(getSession()) ? "yes" : "no"));
		status.add(new Label("lifetime",
			() -> DeviceBoundSessions.get().getCookieLifetime().toSeconds()));
		status.add(new Label("requests", () -> requests));
		status.add(new Label("time", () -> LocalTime.now().truncatedTo(ChronoUnit.SECONDS)));

		add(new AjaxLink<Void>("request")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				requests++;
				target.add(status);
			}
		});
		add(new Link<Void>("signOut")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				getSession().invalidate();
				setResponsePage(getApplication().getHomePage());
			}
		});
	}

	@Override
	protected Class<? extends WicketExamplePage> getIndexPage()
	{
		return AuthenticationIndex.class;
	}

	@Override
	protected String getIndexPath()
	{
		return "authentication";
	}
}
