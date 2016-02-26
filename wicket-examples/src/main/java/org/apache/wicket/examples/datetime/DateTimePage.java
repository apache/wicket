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
package org.apache.wicket.examples.datetime;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.markup.html.form.datetime.DateTimeField;
import org.apache.wicket.extensions.markup.html.form.datetime.StyleTimeConverter;
import org.apache.wicket.extensions.markup.html.form.datetime.TimeField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

/**
 * DateTime example page.
 * 
 */
public class DateTimePage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public DateTimePage()
	{
		add(TimeField.forShortStyle("time1", Model.of(LocalTime.of(22, 15))));
		add(TimeField.forTimeStyle("time2", Model.of(LocalTime.of(22, 15)), "F"));
		add(new TimeField("time3", Model.of(LocalTime.of(22, 15)), new StyleTimeConverter("S")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean use12HourFormat() {
				return false;
			}
		});
		final DateTimeField datetime1 = new DateTimeField("datetime1", Model.of(LocalDateTime.now()));
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		Form<String> form = new Form<>("form");
		add(form.add(datetime1)
				.add(feedback.setOutputMarkupId(true))
				.add(new AjaxButton("submit")
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected void onSubmit(AjaxRequestTarget target)
					{
						form.info(String.format("DateTime was just submitted: %s", datetime1.getModelObject()));
						target.add(feedback);
					}

					@Override
					protected void onError(AjaxRequestTarget target)
					{
						target.add(feedback);
					}
				})
			);
	}
}
