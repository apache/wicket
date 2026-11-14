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
package org.apache.wicket.islands;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.ValidationError;

/**
 * A page with a form island in a form.
 */
public class FormIslandTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	public record Point(int x, int y) implements Serializable
	{
	}

	public record PickerProps(int max) implements Serializable
	{
	}

	final Model<Point> value = Model.of(new Point(1, 2));

	final FormIsland<Point, PickerProps> island;

	final FeedbackPanel feedback;

	int updates;

	static int updatesOnAnyInstance;

	boolean addIslandOnUpdate;

	public FormIslandTestPage()
	{
		feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);

		Form<Void> form = new Form<>("form");
		add(form);

		island = new FormIsland<>("point", "test-island", value, Point.class,
			Model.of(new PickerProps(9)))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Point sanitize(Point point)
			{
				return new Point(Math.min(point.x(), 9), Math.min(point.y(), 9));
			}
		};
		island.setRequired(true);
		island.setLabel(Model.of("Point"));
		island.add(validatable -> {
			if (validatable.getValue().x() == validatable.getValue().y())
			{
				validatable.error(new ValidationError("Not on the diagonal"));
			}
		});
		island.add(new AjaxFormComponentUpdatingBehavior("change")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				updates++;
				updatesOnAnyInstance++;
				if (addIslandOnUpdate)
				{
					target.add(getComponent());
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, RuntimeException e)
			{
				if (addIslandOnUpdate)
				{
					target.add(getComponent());
				}
			}
		});
		form.add(island);

		form.add(new AjaxButton("submit")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				target.add(feedback);
			}
		});
	}
}
