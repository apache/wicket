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
package org.apache.wicket.extensions.yui.calendar;

import java.util.Date;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;

/**
 * Demonstrates components from the wicket-date project and a bunch of locale fiddling.
 */
public class DatesPage2 extends WebPage
{
	private static final long serialVersionUID = 1L;

	/** */
	public Date dateTime;

	/** */
	public Date date;

	/** */
	public Date time;

	/**
	 * Constructor
	 */
	public DatesPage2()
	{
		Form<?> form = new Form<>("form");
		add(form);

		form.add(new DateTimeField("dateTimeField", new PropertyModel<Date>(this, "dateTime")));
		form.add(new DateField("dateField", new PropertyModel<Date>(this, "date")));
		form.add(new TimeField("timeField", new PropertyModel<Date>(this, "time")));
	}
}