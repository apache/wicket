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

import static org.apache.wicket.ajax.AbstractAjaxTimerBehavior.onTimer;

import java.time.ZoneId;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.util.time.Duration;


/**
 * A world clock example page. Demonstrates timer behavior as well as multiple component update.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class WorldClockPage extends BasePage
{
	/**
	 * Constructor
	 */
	public WorldClockPage()
	{
		// create clock components for different timezones
		final Clock la = new Clock("la", ZoneId.of("America/Los_Angeles"));
		final Clock ny = new Clock("ny", ZoneId.of("America/New_York"));
		final Clock moscow = new Clock("moscow", ZoneId.of("Europe/Moscow"));
		final Clock prague = new Clock("prague", ZoneId.of("Europe/Prague"));
		final Clock london = new Clock("london", ZoneId.of("Europe/London"));

		// make components print out id attrs so they can be updated via ajax
		la.setOutputMarkupId(true);
		ny.setOutputMarkupId(true);
		moscow.setOutputMarkupId(true);
		prague.setOutputMarkupId(true);
		london.setOutputMarkupId(true);

		// add the components to the container and add a markup id setter to
		// each component.
		add(la, ny, moscow, prague, london);

		// add the timer behavior to the page and make it update all
		// other components as well
		final AbstractAjaxTimerBehavior timer = onTimer(Duration.seconds(1), target -> target.add(la, ny, moscow, prague, london));
		add(timer);

		add(AjaxLink.onClick("stop", timer::stop));

		add(AjaxLink.onClick("restart", timer::restart));
	}
}
