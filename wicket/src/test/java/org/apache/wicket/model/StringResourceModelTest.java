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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Locale;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.MockPage;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Test cases for the <code>StringResourceModel</code> class.
 * 
 * @author Chris Turner
 */
public class StringResourceModelTest extends TestCase
{
	private WicketTester tester;

	private WebPage page;

	private WeatherStation ws;

	private IModel<WeatherStation> wsModel;

	/**
	 * Create the test case.
	 * 
	 * @param name
	 *            The test name
	 */
	public StringResourceModelTest(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception
	{
		tester = new WicketTester();
		tester.getApplication().getResourceSettings().addStringResourceLoader(
			new BundleStringResourceLoader("org.apache.wicket.model.StringResourceModelTest"));
		page = new MockPage();
		ws = new WeatherStation();
		wsModel = new Model<WeatherStation>(ws);
	}

	@Override
	protected void tearDown() throws Exception
	{
		tester.destroy();
	}

	/**
	 * 
	 * 
	 */
	public void testGetSimpleResource()
	{
		StringResourceModel model = new StringResourceModel("simple.text", page, null);
		Assert.assertEquals("Text should be as expected", "Simple text", model.getString());
		Assert.assertEquals("Text should be as expected", "Simple text", model.getObject());
// Assert.assertEquals("Text should be as expected", "Simple text", model.toString());
	}

	/**
	 * 
	 * 
	 */
	public void testNullResourceKey()
	{
		try
		{
			new StringResourceModel(null, page, null);
			Assert.fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException e)
		{
			// Expected result
		}
	}

	/**
	 * 
	 * 
	 */
	public void testGetSimpleResourceWithKeySubstitution()
	{
		StringResourceModel model = new StringResourceModel("weather.${currentStatus}", page,
			wsModel);
		Assert.assertEquals("Text should be as expected", "It's sunny, wear sunscreen",
			model.getString());
		ws.setCurrentStatus("raining");
		Assert.assertEquals("Text should be as expected", "It's raining, take an umbrella",
			model.getString());
	}

	/**
	 * 
	 * 
	 */
	public void testGetPropertySubstitutedResource()
	{
		StringResourceModel model = new StringResourceModel("weather.message", page, wsModel);
		Assert.assertEquals(
			"Text should be as expected",
			"Weather station \"Europe's main weather station\" reports that the temperature is 25.7 \u00B0C",
			model.getString());
		ws.setCurrentTemperature(11.5);
		Assert.assertEquals(
			"Text should be as expected",
			"Weather station \"Europe's main weather station\" reports that the temperature is 11.5 \u00B0C",
			model.getString());
	}

	/**
	 * 
	 * 
	 */
	public void testSubstitutedPropertyAndParameterResource()
	{
		StringResourceModel model = new StringResourceModel("weather.mixed", page, wsModel,
			new Object[] { new PropertyModel<Double>(wsModel, "currentTemperature"),
					new PropertyModel<String>(wsModel, "units") });
		MessageFormat format = new MessageFormat(
			"Weather station \"Europe''s main weather station\" reports that the temperature is {0} {1}");

		ws.setCurrentTemperature(25.7);
		String expected = format.format(new Object[] { 25.7, "\u00B0C" });
		Assert.assertEquals("Text should be as expected", expected, model.getString());

		ws.setCurrentTemperature(11.5);
		expected = format.format(new Object[] { 11.5, "\u00B0C" });
		Assert.assertEquals("Text should be as expected", expected, model.getString());
	}

	/**
	 * 
	 * 
	 */
	public void testSubstitutionParametersResource()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(2004, Calendar.OCTOBER, 15, 13, 21);
		MessageFormat format = new MessageFormat(
			"The report for {0,date,medium}, shows the temperature as {2,number,###.##} {3} and the weather to be {1}",
			page.getLocale());
		StringResourceModel model = new StringResourceModel("weather.detail", page, wsModel,
			new Object[] { cal.getTime(), "${currentStatus}",
					new PropertyModel<Double>(wsModel, "currentTemperature"),
					new PropertyModel<String>(wsModel, "units") });
		String expected = format.format(new Object[] { cal.getTime(), "sunny", 25.7,
				"\u00B0C" });
		Assert.assertEquals("Text should be as expected", expected, model.getString());
		ws.setCurrentStatus("raining");
		ws.setCurrentTemperature(11.568);
		expected = format.format(new Object[] { cal.getTime(), "raining", 11.568,
				"\u00B0C" });
		Assert.assertEquals("Text should be as expected", expected, model.getString());
	}


	public void testSubstitutionParametersResourceWithSingleQuote() throws Exception
	{
		tester.getWicketSession().setLocale(Locale.ENGLISH);
		StringResourceModel model = new StringResourceModel("with.quote", page, null, new Object[] {
				10, 20});
		assertEquals("2010.00", model.getString());

	}

	/**
	 * 
	 * 
	 */
	public void testUninitialisedLocalizer()
	{
		StringResourceModel model = new StringResourceModel("simple.text", null);
		try
		{
			model.getString();
			Assert.fail("IllegalStateException expected");
		}
		catch (IllegalStateException e)
		{
			// Expected result
		}
	}

	/**
	 * 
	 */
	public void testSetObject()
	{
		try
		{
			StringResourceModel model = new StringResourceModel("simple.text", page, null);
			model.setObject("Some value");
			Assert.fail("UnsupportedOperationException expected");
		}
		catch (Exception e)
		{
			if (!(e instanceof UnsupportedOperationException || e.getCause() instanceof UnsupportedOperationException))
			{
				Assert.fail("UnsupportedOperationException expected");
			}
			// Expected result
		}
	}

	/**
	 * @throws Exception
	 */
	public void testDetachAttachNormalModel() throws Exception
	{
		StringResourceModel model = new StringResourceModel("simple.text", page, wsModel);
		tester.setupRequestAndResponse();
		new WebRequestCycle(tester.getApplication(), tester.getWicketRequest(), tester.getWicketResponse());
		model.getObject();
		Assert.assertNotNull(model.getLocalizer());
		model.detach();
		Assert.assertNull(model.getLocalizer());
	}

	/**
	 * @throws Exception
	 */
	public void testDetachAttachDetachableModel() throws Exception
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
		tester.setupRequestAndResponse();
		new WebRequestCycle(tester.getApplication(), tester.getWicketRequest(), tester.getWicketResponse());
		model.getObject();
		Assert.assertNotNull(model.getLocalizer());
		model.detach();
		// Removed this because getObject() will reattach now...
		Assert.assertNull(model.getLocalizer());
	}

	/**
	 * Inner class used for testing.
	 */
	public class WeatherStation implements Serializable
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
		public void setCurrentStatus(String currentStatus)
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
		public void setCurrentTemperature(double currentTemperature)
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

}
