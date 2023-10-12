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
package org.apache.wicket.markup.html.form.border;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Homepage
 */
public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private String textfield;
	private String datefield;
	private String datefield2;

	String getTextfield()
	{
		return textfield;
	}

	void setTextfield(String textfield)
	{
		this.textfield = textfield;
	}

	String getDatefield()
	{
		return datefield;
	}

	void setDatefield(String datefield)
	{
		this.datefield = datefield;
	}

	String getDatefield2()
	{
		return datefield2;
	}

	void setDatefield2(String datefield)
	{
		datefield2 = datefield;
	}

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

		MyBorder border = new MyBorder("border");
		add(border);

		border.add(new TextField<String>("textfield", new PropertyModel<String>(this, "textfield")));
		border.add(new Label("lbltextfield", new PropertyModel<String>(this, "textfield")));
		border.add(new MyTextField("datefield", new PropertyModel<String>(this, "datefield")).setOutputMarkupId(true));
		border.add(new Label("lbldatefield", new PropertyModel<String>(this, "datefield")));
		border.add(new MyDateField("datefield2", new PropertyModel<String>(this, "datefield2")).setOutputMarkupId(true));
		border.add(new Label("lbldatefield2", new PropertyModel<String>(this, "datefield2")));
	}
}
