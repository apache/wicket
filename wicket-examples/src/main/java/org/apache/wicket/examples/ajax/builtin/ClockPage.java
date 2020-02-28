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

import java.time.Duration;
import java.time.ZoneId;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;


/**
 * A simple clock example page
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ClockPage extends BasePage
{
	/**
	 * Constructor
	 */
	public ClockPage()
	{
		// add the clock component
		Clock clock = new Clock("clock", ZoneId.of("America/Los_Angeles"));
		add(clock);

		// add the ajax behavior which will keep updating the component every 5
		// seconds
		clock.add(new AjaxSelfUpdatingTimerBehavior(Duration.ofSeconds(5)));
	}
}
