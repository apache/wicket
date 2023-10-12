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
package org.apache.wicket.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for the {@link StringResourceModel}.
 * 
 * @author Chris Turner
 */
public class StringResourceModelTest extends WicketTestCase
{
	private WebPage page;

	private WeatherStation ws;

	private IModel<WeatherStation> wsModel;

	/**
	 * @throws Exception
	 */
	@BeforeEach
	void before() throws Exception
	{
		page = new TestPage();
		ws = new WeatherStation();
		wsModel = new Model<>(ws);
	}


	/** */
	@Test
	void getSimpleResource()
	{
		StringResourceModel model = new StringResourceModel("simple.text", page);
		assertEquals("Simple text", model.getString(), "Text should be as expected");
		assertEquals("Simple text", model.getObject(), "Text should be as expected");
	}

	/** */
	@Test
	void getWrappedOnAssignmentResource()
	{
		Label label1 = new Label("resourceModelWithComponent",
			new StringResourceModel("wrappedOnAssignment.text", page));
		page.add(label1);
		assertEquals("Non-wrapped text", label1.getDefaultModelObject(),
			"Text should be as expected");

		Label label2 = new Label("resourceModelWithoutComponent",
			new StringResourceModel("wrappedOnAssignment.text"));
		page.add(label2);
		assertEquals("Wrapped text", label2.getDefaultModelObject(), "Text should be as expected");
	}

	/** */
	@Test
	void nullResourceKey()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			new StringResourceModel(null, page);

		});
	}

	/** */
	@Test
	void getSimpleResourceWithKeySubstitution()
	{
		StringResourceModel model = new StringResourceModel("weather.${currentStatus}", page,
			wsModel);
		assertEquals("It's sunny, wear sunscreen", model.getString(), "Text should be as expected");
		ws.setCurrentStatus("raining");
		assertEquals("It's raining, take an umbrella",
			model.getString(), "Text should be as expected");
		ws.setCurrentStatus(null);
		assertEquals("It's ... i don't know", model.getString(), "Text should be as expected");
	}

	/** */
	@Test
	void getSimpleResourceWithKeySubstitutionForNonString()
	{
		// German uses comma (,) as decimal separator
		Session.get().setLocale(Locale.GERMAN);

		StringResourceModel model = new StringResourceModel("weather.${currentTemperature}", page,
			wsModel);
		assertEquals("Twenty-five dot seven", model.getString(), "Text should be as expected");
	}

	/** */
	@Test
	void getPropertySubstitutedResource()
	{
		tester.getSession().setLocale(Locale.ENGLISH);
		StringResourceModel model = new StringResourceModel("weather.message", page, wsModel);
		assertEquals("Weather station \"Europe's main weather station\" reports that the temperature is 25.7 \u00B0C",
			model.getString(), "Text should be as expected");
		ws.setCurrentTemperature(11.5);
		assertEquals("Weather station \"Europe's main weather station\" reports that the temperature is 11.5 \u00B0C",
			model.getString(), "Text should be as expected");
	}

	/** */
	@Test
	void substitutedPropertyAndParameterResource()
	{
		StringResourceModel model = new StringResourceModel("weather.mixed", page).setModel(wsModel)
			.setParameters(new PropertyModel<Double>(wsModel, "currentTemperature"),
				new PropertyModel<String>(wsModel, "units"));
		MessageFormat format = new MessageFormat(
			"Weather station \"Europe''s main weather station\" reports that the temperature is {0} {1}",
			tester.getSession().getLocale());

		ws.setCurrentTemperature(25.7);
		String expected = format.format(new Object[] { 25.7, "\u00B0C" });
		assertEquals(expected, model.getString(), "Text should be as expected");

		ws.setCurrentTemperature(11.5);
		expected = format.format(new Object[] { 11.5, "\u00B0C" });
		assertEquals(expected, model.getString(), "Text should be as expected");
	}

	/** */
	@Test
	void substitutionParametersResource()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(2004, Calendar.OCTOBER, 15, 13, 21);
		MessageFormat format = new MessageFormat(
			"The report for {0,date,medium}, shows the temperature as {2,number,###.##} {3} and the weather to be {1}",
			page.getLocale());
		StringResourceModel model = new StringResourceModel("weather.detail", page)
			.setModel(wsModel)
			.setParameters(cal.getTime(), "${currentStatus}",
				new PropertyModel<Double>(wsModel, "currentTemperature"),
				new PropertyModel<String>(wsModel, "units"));
		String expected = format.format(new Object[] { cal.getTime(), "sunny", 25.7, "\u00B0C" });
		assertEquals(expected, model.getString(), "Text should be as expected");
		ws.setCurrentStatus("raining");
		ws.setCurrentTemperature(11.568);
		expected = format.format(new Object[] { cal.getTime(), "raining", 11.568, "\u00B0C" });
		assertEquals(expected, model.getString(), "Text should be as expected");
	}

	/** */
	@Test
	void substitutionParametersResourceWithSingleQuote()
	{
		tester.getSession().setLocale(Locale.ENGLISH);
		StringResourceModel model = new StringResourceModel("with.quote", page).setParameters(10,
			20);
		assertEquals("2010.00", model.getString());
	}

	/** */
	@Test
	void textResourceWithSubstitutionAndSingleQuote()
	{
		tester.getSession().setLocale(Locale.ENGLISH);

		StringResourceModel model = new StringResourceModel("with.quote.and.no.substitution", page);
		assertEquals("Let's play in the rain!", model.getString());

		model = new StringResourceModel("with.quote.substitution", page).setParameters("rain!");
		assertEquals("Let's play in the rain!", model.getString());
	}

	/** */
	@Test
	void setObject()
	{
		StringResourceModel model = new StringResourceModel("simple.text", page);
		assertThrows(UnsupportedOperationException.class, () -> {
			model.setObject("Some value");
		});
	}

	/** */
	@Test
	void detachAttachDetachableModel()
	{
		IModel<WeatherStation> wsDetachModel = new LoadableDetachableModel<WeatherStation>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected WeatherStation load()
			{
				return new WeatherStation();
			}
		};

		StringResourceModel model = new StringResourceModel("simple.text", page, wsDetachModel);
		model.getObject();
		assertNotNull(model.getLocalizer());
		model.detach();
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4323
	 */
	@Test
	void detachSubstituteModelFromAssignmentWrapper()
	{
		IModel<WeatherStation> nullOnDetachModel = new Model<WeatherStation>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void detach()
			{
				setObject(null);
			}
		};

		nullOnDetachModel.setObject(ws);
		Label label1 = new Label("resourceModelWithComponent",
			new StringResourceModel("wrappedOnAssignment.text", page, nullOnDetachModel));
		page.add(label1);
		label1.getDefaultModelObject();
		label1.detach();
		assertNull(nullOnDetachModel.getObject());

		nullOnDetachModel.setObject(ws);
		Label label2 = new Label("resourceModelWithoutComponent",
			new StringResourceModel("wrappedOnAssignment.text").setModel(nullOnDetachModel));
		page.add(label2);
		label2.getDefaultModelObject();
		label2.detach();
		assertNull(nullOnDetachModel.getObject());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5176
	 */
	@Test
	void detachEvenNotAttached()
	{
		Wicket5176Model wrappedModel = new Wicket5176Model();
		StringResourceModel stringResourceModel = new StringResourceModel("test")
			.setModel(wrappedModel);
		assertFalse(stringResourceModel.isAttached());
		assertTrue(wrappedModel.isAttached());
		stringResourceModel.detach();
		assertFalse(wrappedModel.isAttached());
	}

	private static class Wicket5176Model implements IModel
	{
		private boolean attached = true;

		@Override
		public Object getObject()
		{
			return null;
		}

		@Override
		public void setObject(Object object)
		{
		}

		@Override
		public void detach()
		{
			attached = false;
		}

		private boolean isAttached()
		{
			return attached;
		}
	}

	/**
	 * Inner class used for testing.
	 */
	public static class WeatherStation implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String name = "Europe's main weather station";

		private String currentStatus = "sunny";

		private double currentTemperature = 25.7;

		/**
		 * @return status
		 */
		public String getCurrentStatus()
		{
			return currentStatus;
		}

		/**
		 * @param currentStatus
		 */
		void setCurrentStatus(String currentStatus)
		{
			this.currentStatus = currentStatus;
		}

		/**
		 * @return current temp
		 */
		public double getCurrentTemperature()
		{
			return currentTemperature;
		}

		/**
		 * @param currentTemperature
		 */
		void setCurrentTemperature(double currentTemperature)
		{
			this.currentTemperature = currentTemperature;
		}

		/**
		 * @return units
		 */
		public String getUnits()
		{
			return "\u00B0C";
		}

		/**
		 * @return name
		 */
		public String getName()
		{
			return name;
		}
	}

	/**
	 * Test page.
	 */
	public static class TestPage extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		TestPage()
		{
		}
	}
}
