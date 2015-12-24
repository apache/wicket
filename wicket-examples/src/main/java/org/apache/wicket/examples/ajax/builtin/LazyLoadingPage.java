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

import java.util.Random;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.util.time.Duration;

@SuppressWarnings({ "javadoc", "serial" })
public class LazyLoadingPage extends BasePage
{
	private Random r = new Random();

	public LazyLoadingPage()
	{
		add(new Link<Void>("startNonblocking")
		{
			@Override
			public void onClick()
			{
				addNonBlockingPanels();
			}
		});
		add(new Link<Void>("startBlocking")
		{
			@Override
			public void onClick()
			{
				addBlockingPanels();
			}
		});

		add(new EmptyPanel("lazy"));
		add(new EmptyPanel("lazy2"));
	}

	private void addNonBlockingPanels()
	{
		RepeatingView rv;
		addOrReplace(rv = new RepeatingView("lazy"));

		for (int i = 0; i < 10; i++)
			rv.add(new AjaxLazyLoadPanel(rv.newChildId())
			{
				private static final long serialVersionUID = 1L;

				private long startTime = System.currentTimeMillis();

				private int seconds = r.nextInt(10);

				@Override
				protected boolean isReadyForReplacement()
				{
					return Duration.milliseconds(System.currentTimeMillis() - startTime)
						.seconds() > seconds;
				}

				@Override
				public Component getLazyLoadComponent(String id)
				{
					return new Label(id, "Lazy Loaded after " + seconds + " seconds");
				}
			});
	}

	private void addBlockingPanels()
	{
		RepeatingView rv;
		addOrReplace(rv = new RepeatingView("lazy2"));

		for (int i = 0; i < 5; i++)
			rv.add(new AjaxLazyLoadPanel(rv.newChildId())
			{
				private static final long serialVersionUID = 1L;

				private int seconds = r.nextInt(5);

				@Override
				public Component getLazyLoadComponent(String markupId)
				{
					try
					{
						System.out.println("Starting sleep");
						Thread.sleep(seconds * 1000);
						System.out.println("Slept " + seconds + " seconds");
					}
					catch (InterruptedException e)
					{
					}
					return new Label(markupId,
						"Lazy loaded after blocking the Wicket thread for " + seconds + " seconds");
				}
			});
	}
}
