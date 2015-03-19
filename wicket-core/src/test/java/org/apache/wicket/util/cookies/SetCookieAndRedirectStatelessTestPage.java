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
package org.apache.wicket.util.cookies;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * Stateless form page which sets a cookie and calls setResponsePage()
 * 
 * @author Bertrand Guay-Paquet
 */
public class SetCookieAndRedirectStatelessTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**	 */
	public static final String cookieName = "CookieValuePersisterStatelessTestPage";

	private IModel<String> inputModel;

	/**
	 * Construct.
	 */
	public SetCookieAndRedirectStatelessTestPage()
	{
		inputModel = new Model<String>();
		Form<Void> form = new StatelessForm<Void>("form")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				CookieUtils utils = new CookieUtils();
				utils.save(cookieName, inputModel.getObject());
				setResponsePage(getApplication().getHomePage());
			}
		};
		add(form);
		form.add(new TextField<String>("input", inputModel));
	}
}