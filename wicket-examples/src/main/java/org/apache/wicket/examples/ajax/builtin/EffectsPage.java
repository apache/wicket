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

import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxChannel.Type;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;

/**
 * Demonstrates ajax effects
 */
public class EffectsPage extends BasePage
{
	private int counter1 = 0;
	private int counter2 = 0;

	/**
	 * @return Value of counter1
	 */
	public int getCounter1()
	{
		return counter1;
	}

	/**
	 * @param counter1
	 *            New value for counter1
	 */
	public void setCounter1(int counter1)
	{
		this.counter1 = counter1;
	}

	/**
	 * @return Value for counter2
	 */
	public int getCounter2()
	{
		return counter2;
	}

	/**
	 * @param counter2
	 *            New value for counter2
	 */
	public void setCounter2(int counter2)
	{
		this.counter2 = counter2;
	}

	/**
	 * Constructor
	 */
	public EffectsPage()
	{
		final Label c1 = new Label("c1", new PropertyModel<Integer>(this, "counter1"));
		c1.setOutputMarkupId(true);
		add(c1);

		final Label c2 = new Label("c2", new PropertyModel<Integer>(this, "counter2"));
		c2.setOutputMarkupId(true);
		add(c2);

		add(new AjaxLink<Void>("c1-link")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				counter1++;
				target.add(c1);
				target.appendJavaScript("new Effect.Shake($('" + c1.getMarkupId() + "'));");
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				attributes.setChannel(new AjaxChannel("effects", Type.DROP));

				super.updateAjaxAttributes(attributes);
			}
		});

		add(new AjaxFallbackLink<Void>("c2-link")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				counter2++;
				if (target != null)
				{
					target.add(c2);
					target.appendJavaScript("new Effect.Highlight($('" + c2.getMarkupId() + "'));");
				}
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				attributes.setChannel(new AjaxChannel("effects", Type.DROP));

				super.updateAjaxAttributes(attributes);
			}
		});
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(OnDomReadyHeaderItem.forScript("jQuery.noConflict();"));
	}

}
