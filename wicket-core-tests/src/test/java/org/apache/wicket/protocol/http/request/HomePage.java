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
package org.apache.wicket.protocol.http.request;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Homepage
 */
public class HomePage extends WebPage
{

	private static final long serialVersionUID = 1L;

	private String text1;
	private String text2;

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public HomePage(final PageParameters parameters)
	{

		// Add the simplest type of label
		add(new Label("message",
			"If you see this message wicket is properly configured and running"));

		Form<Void> f1 = new Form<Void>("form1");
		Form<Void> f2 = new Form<Void>("form2");

		TextField<String> t1 = new TextField<String>("text1", new PropertyModel<String>(this,
			"text1"));
		TextField<String> t2 = new TextField<String>("text2", new PropertyModel<String>(this,
			"text2"));

		Label l1 = new Label("out1", new PropertyModel<String>(this, "text1"));
		Label l2 = new Label("out2", new PropertyModel<String>(this, "text2"));

		add(f1.add(t1)).add(l1);
		add(f2.add(t2)).add(l2);
	}
}
