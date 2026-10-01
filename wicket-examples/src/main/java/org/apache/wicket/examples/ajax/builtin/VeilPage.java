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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.veil.LocalVeilBehavior;
import org.apache.wicket.extensions.ajax.veil.PageVeilBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * Demonstrates {@link PageVeilBehavior} and {@link LocalVeilBehavior}, including nested local
 * veils with their own timings.
 */
public class VeilPage extends BasePage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public VeilPage()
	{
		add(new PageVeilBehavior());

		Label pageCounter = counter(this, "pageCounter");
		add(new SleepingLink("slow", Duration.ofSeconds(1), pageCounter, false));
		add(new SleepingLink("medium", Duration.ofMillis(400), pageCounter, false));
		add(new SleepingLink("fast", Duration.ofMillis(100), pageCounter, false));
		add(new SleepingLink("unveiled", Duration.ofSeconds(1), pageCounter, true));

		WebMarkupContainer outer = new WebMarkupContainer("outer");
		outer.add(new LocalVeilBehavior());
		add(outer);

		Label outerCounter = counter(outer, "outerCounter");
		outer.add(new SleepingLink("outerSlow", Duration.ofSeconds(2), outerCounter, false));

		WebMarkupContainer inner = new WebMarkupContainer("inner");
		inner.add(new LocalVeilBehavior().setSpinnerDelay(Duration.ofMillis(100))
			.setMinimumSpinnerTime(Duration.ofSeconds(1)));
		outer.add(inner);

		Label innerCounter = counter(inner, "innerCounter");
		inner.add(new SleepingLink("innerSlow", Duration.ofSeconds(2), innerCounter, false));
		inner.add(new SleepingLink("innerShort", Duration.ofMillis(200), innerCounter, false));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(CssHeaderItem.forReference(new CssResourceReference(VeilPage.class,
			"VeilPage.css")));
	}

	private static Label counter(MarkupContainer parent, String id)
	{
		Label counter = new Label(id, Model.of(0));
		counter.setOutputMarkupId(true);
		parent.add(counter);
		return counter;
	}

	private static class SleepingLink extends AjaxLink<Void>
	{
		private static final long serialVersionUID = 1L;

		private final Duration duration;

		private final Label counter;

		private final boolean noVeil;

		SleepingLink(String id, Duration duration, Label counter, boolean noVeil)
		{
			super(id);
			this.duration = duration;
			this.counter = counter;
			this.noVeil = noVeil;
		}

		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
		{
			super.updateAjaxAttributes(attributes);
			if (noVeil)
			{
				PageVeilBehavior.noVeil(attributes);
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void onClick(AjaxRequestTarget target)
		{
			sleep(duration);
			IModel<Integer> clicks = (IModel<Integer>)counter.getDefaultModel();
			clicks.setObject(clicks.getObject() + 1);
			target.add(counter);
		}
	}

	private static void sleep(Duration duration)
	{
		try
		{
			Thread.sleep(duration.toMillis());
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}
}
