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

import java.util.Arrays;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.PropertyModel;

/**
 * @author Juergen Donnerstag
 */
public class DropDownChoiceTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private String string = null;

	/**
	 * Construct.
	 * 
	 * @param string
	 *            selected string
	 * @param nullValid
	 *            is null valid
	 */
	public DropDownChoiceTestPage(String string, boolean nullValid)
	{
		this.string = string;

		Form<Void> form = new Form<Void>("form");
		add(form);

		final DropDownChoice<String> ddc = new DropDownChoice<String>("dropdown",
			new PropertyModel<String>(this, "string"), Arrays.asList("A", "B", "C"));
		ddc.setNullValid(nullValid);
		form.add(ddc);
	}
}
