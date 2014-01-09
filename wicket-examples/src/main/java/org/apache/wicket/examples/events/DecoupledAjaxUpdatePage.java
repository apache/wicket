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
package org.apache.wicket.examples.events;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

/**
 * @author igor
 */
public class DecoupledAjaxUpdatePage extends BasePage
{
	private int counter;

	/**
	 * Construct.
	 */
	public DecoupledAjaxUpdatePage()
	{
		// add a counter label
		add(new CounterLabel("label1"));


		// add another counter label inside a container
		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		container.add(new CounterLabel("label2"));

		// add a form
		Form<?> form = new Form<>("form");
		add(form);

		// add the textfield that will update the counter value
		form.add(new TextField<Integer>("counter", new PropertyModel<Integer>(this, "counter"),
			Integer.class).setRequired(true));

		// add button that will broadcast counter update event
		form.add(new AjaxButton("submit")
		{

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				send(getPage(), Broadcast.BREADTH, new CounterUpdate(target));
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
			}

		});
	}

	/**
	 * An event payload that represents a counter update
	 */
	public class CounterUpdate
	{
		private final AjaxRequestTarget target;

		/**
		 * Constructor
		 * 
		 * @param target
		 */
		public CounterUpdate(AjaxRequestTarget target)
		{
			this.target = target;
		}

		/** @return ajax request target */
		public AjaxRequestTarget getTarget()
		{
			return target;
		}
	}

	/**
	 * A label that renders the value of the page's counter variable. Also listens to
	 * {@link CounterUpdate} event and updates itself.
	 * 
	 * @author igor
	 */
	public class CounterLabel extends Label
	{

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public CounterLabel(String id)
		{
			super(id, new PropertyModel<>(DecoupledAjaxUpdatePage.this, "counter"));
			setOutputMarkupId(true);
		}

		/**
		 * @see org.apache.wicket.Component#onEvent(org.apache.wicket.event.IEvent)
		 */
		@Override
		public void onEvent(IEvent<?> event)
		{
			super.onEvent(event);

			// check if this is a counter update event and if so repaint self
			if (event.getPayload() instanceof CounterUpdate)
			{
				CounterUpdate update = (CounterUpdate)event.getPayload();
				update.getTarget().add(this);
			}
		}

	}
}
