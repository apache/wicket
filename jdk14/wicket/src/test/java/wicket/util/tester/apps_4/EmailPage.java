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
package wicket.util.tester.apps_4;

import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.EmailAddressPatternValidator;
import wicket.model.CompoundPropertyModel;

/**
 * @author Juergen Donnerstag
 */
public class EmailPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private String email;
	
	/**
	 * Construct
	 * 
	 */
	public EmailPage()
	{
		Form form = new Form("form");
		form.setModel(new CompoundPropertyModel(this));
		add(form);

		TextField email = new TextField("email");
		email.add(EmailAddressPatternValidator.getInstance());
		form.add(email);
	}

	/**
	 * 
	 * @return xx
	 */
	public String getEmail()
	{
		return this.email;
	}

	/**
	 * 
	 * @param email
	 */
	public void setEmail(final String email)
	{
		this.email = email;
	}
}
