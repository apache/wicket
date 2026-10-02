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
package org.apache.wicket.examples.ajax.builtin;

import java.time.Duration;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.ThemeChoice;
import org.apache.wicket.extensions.markup.html.progress.ProgressBar;
import org.apache.wicket.extensions.theme.Theme;
import org.apache.wicket.extensions.theme.ThemeBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

/**
 * {@link ProgressBar}s on their own: one advanced by an Ajax timer, and an indeterminate one, in a
 * theme the user chooses.
 */
public class ProgressBarPage extends BasePage
{
	private static final long serialVersionUID = 1L;

	private int progress;

	private boolean running;

	private Theme theme = Theme.DEFAULT;

	/**
	 * Constructor.
	 */
	public ProgressBarPage()
	{
		WebMarkupContainer themed = new WebMarkupContainer("themed");
		themed.setOutputMarkupId(true);
		themed.add(new ThemeBehavior(new PropertyModel<>(this, "theme")));
		add(themed);
		themed.add(new ThemeChoice("theme", new PropertyModel<>(this, "theme"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onThemeChanged(AjaxRequestTarget target)
			{
				target.add(themed);
			}
		});

		ProgressBar bar = new ProgressBar("bar", new PropertyModel<>(this, "progress"));
		bar.setOutputMarkupId(true);
		themed.add(bar);
		themed.add(new ProgressBar("indeterminate", Model.of((Integer)null)));

		bar.add(new AbstractAjaxTimerBehavior(Duration.ofMillis(300))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onTimer(AjaxRequestTarget target)
			{
				if (running)
				{
					progress = Math.min(100, progress + 3);
					running = progress < 100;
					target.add(bar);
				}
			}
		});

		themed.add(new AjaxLink<Void>("toggle")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				running = !running && progress < 100;
				target.add(this);
			}
		}.setBody(() -> running ? "Pause" : "Start").setOutputMarkupId(true));

		themed.add(new AjaxLink<Void>("reset")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				progress = 0;
				running = false;
				target.add(bar, themed.get("toggle"));
			}
		});
	}
}
