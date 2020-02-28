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
import java.util.Random;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;

@SuppressWarnings({ "javadoc", "serial" })
public class LazyLoadingPage extends BasePage
{
	private Random r = new Random();
	private WebMarkupContainer nonblocking;
	private WebMarkupContainer blocking;
	private RepeatingView blockingRepeater;
	private RepeatingView nonBlockingRepeater;

	public LazyLoadingPage()
	{
		nonblocking = new WebMarkupContainer("nonblocking");
		nonblocking.setOutputMarkupId(true);
		add(nonblocking);
		
		nonblocking.add(new Link<Void>("start")
		{
			@Override
			public void onClick()
			{
				addNonBlockingPanels();
			}
		});
		nonblocking.add(new AjaxLink<Void>("startAjax")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				addNonBlockingPanels();
			}
		});
		
		nonBlockingRepeater = new RepeatingView("repeater");
		nonblocking.add(nonBlockingRepeater);
		
		blocking = new WebMarkupContainer("blocking");
		blocking.setOutputMarkupId(true);
		add(blocking);
		
		blocking.add(new Link<Void>("start")
		{
			@Override
			public void onClick()
			{
				addBlockingPanels();
			}
		});
		blocking.add(new AjaxLink<Void>("startAjax")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				addBlockingPanels();
			}
		});

		blockingRepeater = new RepeatingView("repeater");
		blocking.add(blockingRepeater);
	}

	private void addNonBlockingPanels()
	{
		nonBlockingRepeater.removeAll();

		for (int i = 0; i < 10; i++)
			nonBlockingRepeater.add(new AjaxLazyLoadPanel<Label>(nonBlockingRepeater.newChildId())
			{
				private static final long serialVersionUID = 1L;

				private long startTime = System.currentTimeMillis();

				private int seconds = r.nextInt(10);

				@Override
				protected boolean isContentReady()
				{
					return Duration.ofMillis(System.currentTimeMillis() - startTime)
						.toSeconds() > seconds;
				}
				
				@Override
				protected Duration getUpdateInterval()
				{
					return Duration.ofMillis(seconds * 1000 / 10);
				}

				@Override
				public Label getLazyLoadComponent(String id)
				{
					return new Label(id, "Lazy Loaded after " + seconds + " seconds");
				}
			});
		
		getRequestCycle().find(AjaxRequestTarget.class).ifPresent(t -> t.add(nonblocking));
	}

	private void addBlockingPanels()
	{
		blockingRepeater.removeAll();

		for (int i = 0; i < 5; i++)
			blockingRepeater.add(new AjaxLazyLoadPanel<Label>(blockingRepeater.newChildId())
			{
				private static final long serialVersionUID = 1L;

				private int seconds = r.nextInt(5);

				@Override
				public Label getLazyLoadComponent(String markupId)
				{
					try
					{
						Thread.sleep(seconds * 1000);
					}
					catch (InterruptedException e)
					{
					}
					return new Label(markupId,
						"Lazy loaded after blocking the Wicket thread for " + seconds + " seconds");
				}
			});
		
		getRequestCycle().find(AjaxRequestTarget.class).ifPresent(t -> t.add(blocking));
	}
}
