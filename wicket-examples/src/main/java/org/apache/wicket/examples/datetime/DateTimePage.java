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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.wicket.Session;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.examples.forminput.FormInputApplication;
import org.apache.wicket.extensions.markup.html.form.datetime.LocalDateTextField;
import org.apache.wicket.extensions.markup.html.form.datetime.LocalDateTimeField;
import org.apache.wicket.extensions.markup.html.form.datetime.LocalDateTimeTextField;
import org.apache.wicket.extensions.markup.html.form.datetime.TimeField;
import org.apache.wicket.extensions.markup.html.form.datetime.ZonedDateTimeField;
import org.apache.wicket.extensions.markup.html.form.datetime.ZonedToLocalDateTimeModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.http.WebRequest;

/**
 * DateTime example page.
 * 
 */
public class DateTimePage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	private ZoneId clientZone;

	private ZoneId targetZone = ZoneId.of("UTC+8");

	@SuppressWarnings("unused")
	private LocalTime time1 = LocalTime.of(22, 15);

	@SuppressWarnings("unused")
	private LocalTime time2 = LocalTime.of(22, 15);

	@SuppressWarnings("unused")
	private LocalDate date = LocalDate.now();

	@SuppressWarnings("unused")
	private LocalDateTime dateTime0 = LocalDateTime.now();

	@SuppressWarnings("unused")
	private ZonedDateTime dateTime1 = LocalDateTime.now().atZone(targetZone);

	@SuppressWarnings("unused")
	private ZonedDateTime dateTime2 = LocalDateTime.now().atZone(targetZone);

	@SuppressWarnings("unused")
	private ZonedDateTime dateTime3 = ZonedDateTime.now();

	/**
	 * Constructor.
	 */
	public DateTimePage()
	{
		Form<String> form = new Form<>("form");
		this.add(form);

		form.add(new ZoneDropDownChoice("zoneSelect"));

		// Dropdown for selecting locale
		form.add(new LocaleDropDownChoice("localeSelect"));

		// Link to return to default locale
		form.add(new Link<Void>("defaultLocaleLink")
		{
			private static final long serialVersionUID = 1L;

			public void onClick()
			{
				WebRequest request = (WebRequest)getRequest();
				getSession().setLocale(request.getLocale());
			}
		});

		form.add(new TimeField("time1", new PropertyModel<>(this, "time1"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean use12HourFormat()
			{
				return true;
			}
		});

		form.add(new TimeField("time2", new PropertyModel<>(this, "time2"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean use12HourFormat()
			{
				return false;
			}
		});

		final LocalDateTextField dateField = new LocalDateTextField("date", new PropertyModel<>(this, "date"), "dd-MM-yyyy", "d-M-yyyy");
		form.add(dateField);

		final LocalDateTimeField datetimeField0 = new LocalDateTimeField("datetime0",
			new PropertyModel<>(this, "dateTime0"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected LocalTime getDefaultTime()
			{
				return LocalTime.of(0, 0);
			}
		};
		form.add(datetimeField0);

		IModel<ZonedDateTime> zonedDateTime1 = new PropertyModel<>(this, "dateTime1");
		final LocalDateTimeField datetimeField1 = new LocalDateTimeField("datetime1",
			new ZonedToLocalDateTimeModel(zonedDateTime1)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected ZoneId getClientTimeZone()
				{
					return clientZone;
				}

				@Override
				protected ZoneId getTargetTimeZone()
				{
					return targetZone;
				}
			});
		form.add(datetimeField1);
		form.add(new Label("datetime1-label", zonedDateTime1));

		IModel<ZonedDateTime> zonedDateTime2 = new PropertyModel<>(this, "dateTime2");
		LocalDateTimeTextField datetimeField2 = new LocalDateTimeTextField("datetime2",
			new ZonedToLocalDateTimeModel(zonedDateTime2)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected ZoneId getClientTimeZone()
				{
					return clientZone;
				}

				@Override
				protected ZoneId getTargetTimeZone()
				{
					return targetZone;
				}
			}, FormatStyle.SHORT, FormatStyle.SHORT);
		form.add(datetimeField2);
		form.add(new Label("datetime2-label", zonedDateTime2));

		final ZonedDateTimeField datetimeField3 = new ZonedDateTimeField("datetime3",
			new PropertyModel<>(this, "dateTime3"));
		form.add(datetimeField3);

		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		form.add(feedback);

		form.add(new Button("submit"));
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		clientZone = ((WebClientInfo)Session.get().getClientInfo()).getProperties().getTimeZone()
			.toZoneId();
	}

	/**
	 * Choice for a locale.
	 */
	private final class LocaleChoiceRenderer extends ChoiceRenderer<Locale>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(Locale locale)
		{
			return locale.getDisplayName(getLocale());
		}
	}

	/**
	 * Dropdown with Locales.
	 */
	private final class LocaleDropDownChoice extends DropDownChoice<Locale>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 */
		public LocaleDropDownChoice(String id)
		{
			super(id, FormInputApplication.LOCALES, new LocaleChoiceRenderer());

			setModel(new PropertyModel<>(this, "session.locale"));

			add(new FormComponentUpdatingBehavior()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate()
				{
					setResponsePage(getPage().getClass());
				}
			});
		}
	}

	private class ZoneDropDownChoice extends DropDownChoice<ZoneId>
	{
		private static final long serialVersionUID = 1L;

		public ZoneDropDownChoice(String id)
		{
			super(id, new IModel<List<ZoneId>>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public List<ZoneId> getObject()
				{
					return ZoneId.getAvailableZoneIds().stream().map(id -> ZoneId.of(id))
						.collect(Collectors.toList());
				}
			});

			setModel(new PropertyModel<ZoneId>(DateTimePage.this, "clientZone"));

			setChoiceRenderer(new IChoiceRenderer<ZoneId>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public Object getDisplayValue(ZoneId object)
				{
					String name = object.getDisplayName(TextStyle.FULL, getLocale());

					ZoneOffset offset = LocalDateTime.now().atZone(object).getOffset();

					return name + offset;
				}

				@Override
				public String getIdValue(ZoneId object, int index)
				{
					return object.getId();
				}

				@Override
				public ZoneId getObject(String id, IModel<? extends List<? extends ZoneId>> choices)
				{
					return ZoneId.of(id);
				}
			});

			add(new FormComponentUpdatingBehavior()
			{
				private static final long serialVersionUID = 1L;

				protected void onUpdate()
				{
					// clear raw input of all inputs so that values are reformatted
					getForm().clearInput();
				};
			});
		}
	}
}
