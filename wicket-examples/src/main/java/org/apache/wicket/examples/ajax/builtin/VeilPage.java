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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.veil.LocalVeilBehavior;
import org.apache.wicket.extensions.ajax.veil.PageVeilBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * Demonstrates {@link PageVeilBehavior} and {@link LocalVeilBehavior}, including nested local
 * veils with their own timings, and the cases a veil has to hold in: a delegated behavior, and
 * hosts that scroll, are positioned or sit under a sticky header.
 */
public class VeilPage extends BasePage
{
	private static final long serialVersionUID = 1L;

	private static final int ROWS = 3;

	private final int[] rowClicks = new int[ROWS];

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

		addDelegatedRows();
		addObjectMemberId();
		addScroller();
		addPositionedHost();
		addStickyHeader();
	}

	private void addDelegatedRows()
	{
		WebMarkupContainer rows = new WebMarkupContainer("rows");
		add(rows);
		Loop rowLoop = new Loop("row", ROWS)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(LoopItem item)
			{
				int index = item.getIndex();
				item.add(new LocalVeilBehavior());
				item.add(AttributeModifier.replace("data-row", index));
				item.add(new Label("rowNumber", index + 1));
				item.add(new Label("rowCounter", () -> rowClicks[index]));
			}
		};
		rows.add(rowLoop);

		rows.add(new AjaxEventBehavior("click")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				attributes.setChildSelector("li");
				attributes.getDynamicExtraParameters()
					.add("return {row: attrs.event.target.closest('li').dataset.row};");
			}

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				int index = getRequest().getRequestParameters()
					.getParameterValue("row")
					.toInt(-1);
				if (index >= 0 && index < ROWS)
				{
					sleep(Duration.ofSeconds(1));
					rowClicks[index]++;
					target.add(rowLoop.get(Integer.toString(index)));
				}
			}
		});
	}

	private void addObjectMemberId()
	{
		WebMarkupContainer constructor = new WebMarkupContainer("constructor");
		constructor.setMarkupId("constructor");
		constructor.add(new LocalVeilBehavior());
		add(constructor);

		Label counter = counter(constructor, "constructorCounter");
		constructor.add(new SleepingLink("constructorSlow", Duration.ofSeconds(1), counter, false));
	}

	private void addScroller()
	{
		WebMarkupContainer scroller = new WebMarkupContainer("scroller");
		scroller.add(new LocalVeilBehavior());
		add(scroller);

		Label counter = counter(this, "scrollerCounter");
		scroller.add(new Loop("line", 30)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(LoopItem item)
			{
				item.add(new Label("lineNumber", item.getIndex() + 1));
				item.add(new SleepingLink("lineSlow", Duration.ofSeconds(2), counter, false));
			}
		});
	}

	private void addPositionedHost()
	{
		WebMarkupContainer card = new WebMarkupContainer("positioned");
		card.add(new LocalVeilBehavior());
		add(card);

		Label counter = counter(card, "positionedCounter");
		card.add(new SleepingLink("positionedSlow", Duration.ofSeconds(2), counter, false));
	}

	private void addStickyHeader()
	{
		WebMarkupContainer underHeader = new WebMarkupContainer("underHeader");
		underHeader.add(new LocalVeilBehavior());
		add(underHeader);

		Label counter = counter(underHeader, "underHeaderCounter");
		underHeader.add(new SleepingLink("underHeaderSlow", Duration.ofSeconds(3), counter, false));
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
