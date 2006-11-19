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
package wicket;

import java.util.Arrays;

import wicket.markup.html.WebPage;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;

/**
 * 
 */
public class MyMockPage extends WebPage
{
	private static final long serialVersionUID = 1L;
	
	DropDownChoice drop1;
	DropDownChoice drop2;

	/**
	 * Construct.
	 */
	public MyMockPage()
	{
		final Form form = new Form("form");
		add(form);

		String[] choices = { "choice1", "choice2" };
		drop1 = new DropDownChoice("drop1", Arrays.asList(choices));
		drop2 = new DropDownChoice("drop2", Arrays.asList(choices));
		
		drop1.setNullValid(true);
		drop2.setNullValid(true);
		
		form.add(drop1);
		form.add(drop2);
	}
}