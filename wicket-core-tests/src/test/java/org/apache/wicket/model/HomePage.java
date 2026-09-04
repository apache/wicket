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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.FormComponentFeedbackBorder;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;

/**
 * Homepage: A simple test for CompoundPropertyModel and id's like "A.B"
 */
public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private final Booking booking = new Booking();

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public HomePage(final PageParameters parameters)
	{
		add(new FeedbackPanel("feedback"));
		add(new BookingForm("bookingForm"));
	}

	private class BookingForm extends Form<Booking>
	{
		private static final long serialVersionUID = 1L;

		String name;

		BookingForm(String s)
		{
			super(s);
			setDefaultModel(new CompoundPropertyModel<Booking>(booking));

			TextField<String> name = new TextField<String>("partyDetails.name");
			name.setRequired(Boolean.TRUE);
			name.add(new StringValidator(1, 30));

			FormComponentFeedbackBorder nameBorder = new FormComponentFeedbackBorder("nameBorder");
			add(nameBorder);
			nameBorder.add(name);
		}
	}

	private class Booking implements Serializable
	{
		private static final long serialVersionUID = 1L;

		PartyDetails partyDetails = new PartyDetails();

		private Booking()
		{
		}

		public PartyDetails getPartyDetails()
		{
			return partyDetails;
		}

		public void setPartyDetails(PartyDetails partyDetails)
		{
			this.partyDetails = partyDetails;
		}
	}

	private class PartyDetails implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String name;

		private PartyDetails()
		{
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}
	}
}
