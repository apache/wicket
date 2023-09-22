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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.markup.html.WebPage;

/**
 * Test page for checking markup of radiogroups.
 * 
 * @author Martijn Dashorst
 */
public class RadioGroupTestPage3 extends WebPage
{
	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public RadioGroupTestPage3()
	{
		Form<Void> form = new Form<Void>("form");
		RadioGroup<?> radio = new RadioGroup<Object>("radio");
		radio.add(new Radio<Object>("check1"));
		radio.add(new Radio<Object>("check2"));
		form.add(radio);
		add(form);
	}
}
